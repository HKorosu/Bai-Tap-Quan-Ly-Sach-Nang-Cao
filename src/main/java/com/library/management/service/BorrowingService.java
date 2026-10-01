package com.library.management.service;

import com.library.management.dto.BorrowLineForm;
import com.library.management.dto.ReturnLineForm;
import com.library.management.model.*;
import com.library.management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BorrowingService {
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final BorrowingRepository borrowingRepository;
    private final FinePolicyRepository finePolicyRepository;
    private final FineCalculationService fineCalculationService;

    public BorrowingService(BookRepository bookRepository, MemberRepository memberRepository,
                            BorrowingRepository borrowingRepository, FinePolicyRepository finePolicyRepository,
                            FineCalculationService fineCalculationService) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.borrowingRepository = borrowingRepository;
        this.finePolicyRepository = finePolicyRepository;
        this.fineCalculationService = fineCalculationService;
    }

    @Transactional
    public Borrowing create(Long memberId, LocalDate dueDate, List<BorrowLineForm> lines) {
        LocalDate today = LocalDate.now();
        if (dueDate == null || dueDate.isBefore(today)) throw new IllegalArgumentException("Hạn trả phải từ hôm nay trở đi.");
        Map<Long, Integer> requested = new LinkedHashMap<>();
        for (BorrowLineForm line : lines) {
            if (line.getBookId() == null && line.getQuantity() == 0) continue;
            if (line.getBookId() == null || line.getQuantity() <= 0) {
                throw new IllegalArgumentException("Mỗi dòng sách đã chọn cần có số lượng lớn hơn 0.");
            }
            if (requested.putIfAbsent(line.getBookId(), line.getQuantity()) != null) {
                throw new IllegalArgumentException("Không được chọn trùng một đầu sách trong cùng phiếu.");
            }
        }
        if (requested.isEmpty()) throw new IllegalArgumentException("Phiếu mượn phải có ít nhất một đầu sách.");

        Member member = memberRepository.findLockedById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy độc giả."));
        List<Borrowing> memberBorrowings = borrowingRepository.findByMemberId(memberId);
        int currentlyBorrowed = 0;
        for (Borrowing existing : memberBorrowings) {
            if (existing.getStatus() == BorrowingStatus.CANCELLED) continue;
            if (existing.getUnpaidFineAmount().signum() > 0) {
                throw new IllegalStateException("Độc giả còn phí phạt chưa thanh toán, không thể mượn sách.");
            }
            if (!existing.isFullyReturned()) {
                if (today.isAfter(existing.getDueDate())) {
                    throw new IllegalStateException("Độc giả đang có phiếu quá hạn, không thể tạo phiếu mới.");
                }
                currentlyBorrowed += existing.getDetails().stream()
                        .mapToInt(BorrowingDetail::getRemainingQuantity).sum();
            }
        }
        if (currentlyBorrowed + requested.values().stream().mapToInt(Integer::intValue).sum() > 5) {
            throw new IllegalStateException("Mỗi độc giả chỉ được mượn tối đa 5 cuốn chưa trả.");
        }

        Map<Long, Book> lockedBooks = new LinkedHashMap<>();
        for (Long bookId : requested.keySet().stream().sorted().toList()) {
            Book book = bookRepository.findLockedById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy một đầu sách đã chọn."));
            if (book.getAvailableQuantity() < requested.get(bookId)) {
                throw new IllegalStateException("Sách \"" + book.getTitle() + "\" không đủ số lượng còn lại.");
            }
            lockedBooks.put(bookId, book);
        }

        Borrowing borrowing = new Borrowing(member, today, dueDate);
        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {
            Book book = lockedBooks.get(entry.getKey());
            book.borrow(entry.getValue());
            borrowing.addDetail(new BorrowingDetail(book, entry.getValue()));
        }
        return borrowingRepository.save(borrowing);
    }

    @Transactional
    public Borrowing returnBooks(Long borrowingId, List<ReturnLineForm> lines) {
        Borrowing borrowing = lockMemberAndBorrowing(borrowingId);
        if (borrowing.getStatus() == BorrowingStatus.RETURNED
                || borrowing.getStatus() == BorrowingStatus.FINE_PENDING
                || borrowing.getStatus() == BorrowingStatus.FINE_PAID
                || borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new IllegalStateException("Phiếu này không còn sách để trả.");
        }
        Map<Long, Integer> requested = new LinkedHashMap<>();
        for (ReturnLineForm line : lines) {
            if (line.getQuantityToReturn() == 0) continue;
            if (line.getQuantityToReturn() < 0) throw new IllegalArgumentException("Số lượng trả không được âm.");
            if (requested.putIfAbsent(line.getDetailId(), line.getQuantityToReturn()) != null) {
                throw new IllegalArgumentException("Dòng sách bị lặp trong yêu cầu trả.");
            }
        }
        if (requested.isEmpty()) throw new IllegalArgumentException("Vui lòng nhập số lượng cần trả.");

        Map<Long, BorrowingDetail> details = borrowing.getDetails().stream()
                .collect(Collectors.toMap(BorrowingDetail::getId, Function.identity()));
        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {
            BorrowingDetail detail = details.get(entry.getKey());
            if (detail == null || entry.getValue() > detail.getRemainingQuantity()) {
                throw new IllegalArgumentException("Số lượng trả vượt quá số sách đang mượn.");
            }
        }
        Map<Long, Book> lockedBooks = new HashMap<>();
        for (Long bookId : requested.keySet().stream().map(details::get).map(d -> d.getBook().getId()).distinct().sorted().toList()) {
            Book book = bookRepository.findLockedById(bookId)
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy sách của phiếu mượn."));
            lockedBooks.put(bookId, book);
        }

        LocalDate today = LocalDate.now();
        FinePolicy policy = finePolicyRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình chính sách phí phạt."));
        for (Map.Entry<Long, Integer> entry : requested.entrySet()) {
            BorrowingDetail detail = details.get(entry.getKey());
            int quantity = entry.getValue();
            FineCalculationService.FineQuote quote = fineCalculationService.calculate(
                    detail, borrowing.getDueDate(), today, quantity, policy);
            detail.returnBooks(quantity, today, quote.lateDays(), quote.fineAmount());
            lockedBooks.get(detail.getBook().getId()).restore(quantity);
            borrowing.addFine(quote.fineAmount());
        }
        borrowing.refreshStatus(today);
        return borrowing;
    }

    @Transactional
    public Borrowing cancel(Long borrowingId) {
        Borrowing borrowing = lockMemberAndBorrowing(borrowingId);
        borrowing.cancel();
        for (BorrowingDetail detail : borrowing.getDetails().stream()
                .sorted(Comparator.comparing(detail -> detail.getBook().getId())).toList()) {
            int remaining = detail.getRemainingQuantity();
            if (remaining > 0) bookRepository.findLockedById(detail.getBook().getId())
                    .orElseThrow(() -> new IllegalStateException("Không tìm thấy sách của phiếu mượn."))
                    .restore(remaining);
        }
        return borrowing;
    }

    @Transactional
    public Borrowing payFine(Long borrowingId, BigDecimal amount, String method, String note) {
        Borrowing borrowing = lockMemberAndBorrowing(borrowingId);
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        if (amount.compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new IllegalArgumentException("Số tiền thanh toán vượt quá phí phạt còn phải thu.");
        }
        if (method == null || method.isBlank()) throw new IllegalArgumentException("Vui lòng chọn phương thức thanh toán.");
        borrowing.addPayment(new FinePayment(amount, LocalDate.now(), method.trim(), note));
        borrowing.refreshStatus(LocalDate.now());
        return borrowing;
    }

    @Transactional
    public Borrowing waiveFine(Long borrowingId, BigDecimal amount, String reason, String approvedBy) {
        Borrowing borrowing = lockMemberAndBorrowing(borrowingId);
        if (amount == null || amount.signum() <= 0 || amount.compareTo(borrowing.getUnpaidFineAmount()) > 0) {
            throw new IllegalArgumentException("Số tiền miễn giảm phải lớn hơn 0 và không vượt quá phí còn phải thu.");
        }
        if (reason == null || reason.isBlank() || approvedBy == null || approvedBy.isBlank()) {
            throw new IllegalArgumentException("Lý do và người duyệt miễn giảm là bắt buộc.");
        }
        borrowing.addWaiver(new FineWaiver(amount, reason.trim(), approvedBy.trim(), LocalDate.now()));
        borrowing.refreshStatus(LocalDate.now());
        return borrowing;
    }

    private Borrowing lockedBorrowing(Long id) {
        return borrowingRepository.findLockedWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));
    }

    private Borrowing lockMemberAndBorrowing(Long borrowingId) {
        Borrowing reference = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));
        lockMember(reference.getMember().getId());
        return lockedBorrowing(borrowingId);
    }

    private Member lockMember(Long id) {
        return memberRepository.findLockedById(id)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy độc giả của phiếu mượn."));
    }
}
