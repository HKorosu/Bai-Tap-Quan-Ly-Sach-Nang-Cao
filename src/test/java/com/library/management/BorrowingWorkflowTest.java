package com.library.management;

import com.library.management.dto.BorrowLineForm;
import com.library.management.dto.ReturnLineForm;
import com.library.management.model.*;
import com.library.management.repository.*;
import com.library.management.service.BorrowingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BorrowingWorkflowTest {
    @Autowired BorrowingService borrowingService;
    @Autowired BookRepository bookRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired BorrowingRepository borrowingRepository;
    @Autowired CategoryRepository categoryRepository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void createsBorrowingAndDeductsInventory() {
        Book book = bookRepository.findAllByOrderByTitleAsc().getFirst();
        Member member = memberRepository.save(new Member("Test Borrower", "test-" + System.nanoTime() + "@example.com", ""));
        int before = book.getAvailableQuantity();
        BorrowLineForm line = line(book.getId(), 2);
        Borrowing borrowing = borrowingService.create(member.getId(), LocalDate.now().plusDays(10), List.of(line));
        assertEquals(1, borrowing.getDetails().size());
        assertEquals(before - 2, bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity());
    }

    @Test
    @Transactional
    void insufficientInventoryDoesNotCreateBorrowingOrChangeStock() {
        Category category = categoryRepository.findAllByOrderByNameAsc().getFirst();
        Book availableBook = bookRepository.save(new Book("Rollback available book", "Test Author",
                "ROLLBACK-AVAILABLE-" + System.nanoTime(), category, 2));
        Book unavailableBook = bookRepository.save(new Book("Rollback unavailable book", "Test Author",
                "ROLLBACK-EMPTY-" + System.nanoTime(), category, 0));
        Member member = memberRepository.save(new Member("Rollback Borrower", "rollback-" + System.nanoTime() + "@example.com", ""));
        long count = borrowingRepository.count();
        assertThrows(IllegalStateException.class,
                () -> borrowingService.create(member.getId(), LocalDate.now().plusDays(10),
                        List.of(line(availableBook.getId(), 1), line(unavailableBook.getId(), 1))));
        assertEquals(2, bookRepository.findById(availableBook.getId()).orElseThrow().getAvailableQuantity());
        assertEquals(0, bookRepository.findById(unavailableBook.getId()).orElseThrow().getAvailableQuantity());
        assertEquals(count, borrowingRepository.count());
    }

    @Test
    void returnsBooksAndCreatesFineForLatePartialReturn() {
        Book book = bookRepository.findAllByOrderByTitleAsc().getFirst();
        Member member = memberRepository.save(new Member("Late Borrower", "late-" + System.nanoTime() + "@example.com", ""));
        int before = book.getAvailableQuantity();
        Borrowing borrowing = borrowingService.create(member.getId(), LocalDate.now().plusDays(10),
                List.of(line(book.getId(), 2)));
        BorrowingDetail detail = borrowing.getDetails().getFirst();
        jdbcTemplate.update("UPDATE borrowings SET due_date = ? WHERE id = ?",
                LocalDate.now().minusDays(3), borrowing.getId());
        ReturnLineForm returned = new ReturnLineForm();
        returned.setDetailId(detail.getId());
        returned.setQuantityToReturn(1);
        Borrowing updated = borrowingService.returnBooks(borrowing.getId(), List.of(returned));
        assertEquals(1, updated.getDetails().getFirst().getReturnedQuantity());
        assertEquals(new BigDecimal("3000.00"), updated.getTotalFineAmount());
        assertEquals(BorrowingStatus.OVERDUE, updated.getStatus());
        assertEquals(before - 1, bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity());

        assertThrows(IllegalArgumentException.class, () -> borrowingService.payFine(
                borrowing.getId(), new BigDecimal("3000.01"), "CASH", ""));
        Borrowing afterPayment = borrowingService.payFine(
                borrowing.getId(), new BigDecimal("1000"), "CASH", "");
        assertEquals(new BigDecimal("1000.00"), afterPayment.getPaidFineAmount());
        assertEquals(new BigDecimal("2000.00"), afterPayment.getUnpaidFineAmount());
        Member blockedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThrows(IllegalStateException.class, () -> borrowingService.create(blockedMember.getId(),
                LocalDate.now().plusDays(10), List.of(line(book.getId(), 1))));
    }

    @Test
    void returnsAllBooksOnTimeWithoutFine() {
        Book book = bookRepository.findAllByOrderByTitleAsc().getFirst();
        Member member = memberRepository.save(new Member("On-time Borrower", "ontime-" + System.nanoTime() + "@example.com", ""));
        int before = book.getAvailableQuantity();
        Borrowing borrowing = borrowingService.create(member.getId(), LocalDate.now().plusDays(10),
                List.of(line(book.getId(), 1)));
        ReturnLineForm returned = new ReturnLineForm();
        returned.setDetailId(borrowing.getDetails().getFirst().getId());
        returned.setQuantityToReturn(1);
        Borrowing updated = borrowingService.returnBooks(borrowing.getId(), List.of(returned));
        assertEquals(BorrowingStatus.RETURNED, updated.getStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(updated.getTotalFineAmount()));
        assertEquals(before, bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity());
    }

    @Test
    void cancellationRestoresAllBorrowedInventory() {
        Book book = bookRepository.findAllByOrderByTitleAsc().getFirst();
        Member member = memberRepository.save(new Member("Cancel Borrower", "cancel-" + System.nanoTime() + "@example.com", ""));
        int before = book.getAvailableQuantity();
        Borrowing borrowing = borrowingService.create(member.getId(), LocalDate.now().plusDays(10),
                List.of(line(book.getId(), 1)));
        Borrowing cancelled = borrowingService.cancel(borrowing.getId());
        assertEquals(BorrowingStatus.CANCELLED, cancelled.getStatus());
        assertEquals(before, bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity());
    }

    @Test
    void twoConcurrentRequestsCannotBorrowTheLastCopy() throws Exception {
        Category category = categoryRepository.findAllByOrderByNameAsc().getFirst();
        Book book = bookRepository.save(new Book("Concurrent test book", "Test Author",
                "CONCURRENT-" + System.nanoTime(), category, 1));
        Member first = memberRepository.save(new Member("Concurrent One", "concurrent-one-" + System.nanoTime() + "@example.com", ""));
        Member second = memberRepository.save(new Member("Concurrent Two", "concurrent-two-" + System.nanoTime() + "@example.com", ""));
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> firstAttempt = executor.submit(() -> borrowWhenReleased(start, successes, first, book));
            Future<?> secondAttempt = executor.submit(() -> borrowWhenReleased(start, successes, second, book));
            start.countDown();
            firstAttempt.get();
            secondAttempt.get();
        } finally {
            executor.shutdownNow();
        }
        assertEquals(1, successes.get());
        assertEquals(0, bookRepository.findById(book.getId()).orElseThrow().getAvailableQuantity());
    }

    private BorrowLineForm line(Long bookId, int quantity) {
        BorrowLineForm line = new BorrowLineForm();
        line.setBookId(bookId);
        line.setQuantity(quantity);
        return line;
    }

    private void borrowWhenReleased(CountDownLatch start, AtomicInteger successes, Member member, Book book) {
        try {
            start.await();
            borrowingService.create(member.getId(), LocalDate.now().plusDays(10), List.of(line(book.getId(), 1)));
            successes.incrementAndGet();
        } catch (IllegalStateException expectedForSecondRequest) {
            // Only one request can reserve the single available copy.
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(exception);
        }
    }
}
