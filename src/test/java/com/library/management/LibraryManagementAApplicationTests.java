package com.library.management;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import com.library.management.dto.BorrowLineForm;
import com.library.management.model.Book;
import com.library.management.model.Borrowing;
import com.library.management.model.Member;
import com.library.management.repository.BookRepository;
import com.library.management.repository.MemberRepository;
import com.library.management.service.BorrowingService;
import java.time.LocalDate;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryManagementAApplicationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired private BookRepository bookRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private BorrowingService borrowingService;

    @Test
    void contextLoads() {
    }

    @Test
    void rendersMainThymeleafPages() throws Exception {
        mockMvc.perform(get("/books")).andExpect(status().isOk());
        mockMvc.perform(get("/books/new")).andExpect(status().isOk());
        mockMvc.perform(get("/members")).andExpect(status().isOk());
        mockMvc.perform(get("/borrowings")).andExpect(status().isOk());
        mockMvc.perform(get("/borrowings/new")).andExpect(status().isOk());
        mockMvc.perform(get("/fine-policies")).andExpect(status().isOk());
        mockMvc.perform(get("/reports/overdue")).andExpect(status().isOk());
    }

    @Test
    void rendersBorrowingDetailReturnAndPaymentPages() throws Exception {
        Book book = bookRepository.findAllByOrderByTitleAsc().getFirst();
        Member member = memberRepository.save(new Member("Page Test", "page-" + System.nanoTime() + "@example.com", ""));
        BorrowLineForm line = new BorrowLineForm();
        line.setBookId(book.getId());
        line.setQuantity(1);
        Borrowing borrowing = borrowingService.create(member.getId(), LocalDate.now().plusDays(10), List.of(line));
        mockMvc.perform(get("/borrowings/{id}", borrowing.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/borrowings/{id}/return", borrowing.getId())).andExpect(status().isOk());
        mockMvc.perform(get("/borrowings/{id}/payments/new", borrowing.getId())).andExpect(status().isOk());
    }
}
