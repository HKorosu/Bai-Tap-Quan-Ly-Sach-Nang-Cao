package com.library.management.web;

import com.library.management.dto.BookForm;
import com.library.management.model.Book;
import com.library.management.model.Category;
import com.library.management.repository.BookRepository;
import com.library.management.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class BookController {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    public BookController(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/books")
    public String books(@RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        var books = bookRepository.findAllByOrderByTitleAsc();
        if (q != null && !q.isBlank()) {
            String term = q.trim().toLowerCase();
            books = books.stream().filter(book -> book.getTitle().toLowerCase().contains(term)
                    || book.getAuthor().toLowerCase().contains(term)
                    || book.getIsbn().toLowerCase().contains(term)
                    || book.getCategory().getName().toLowerCase().contains(term)).toList();
        }
        int pageSize = 10;
        int pageCount = Math.max(1, (books.size() + pageSize - 1) / pageSize);
        int currentPage = Math.max(0, Math.min(page, pageCount - 1));
        int start = currentPage * pageSize;
        model.addAttribute("books", books.subList(start, Math.min(start + pageSize, books.size())));
        model.addAttribute("page", currentPage);
        model.addAttribute("pageCount", pageCount);
        model.addAttribute("q", q);
        return "books";
    }

    @GetMapping("/books/new")
    public String newBook(Model model) {
        model.addAttribute("bookForm", new BookForm());
        model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
        return "book-form";
    }

    @PostMapping("/books")
    public String createBook(@Valid @ModelAttribute BookForm bookForm, BindingResult result, Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryRepository.findAllByOrderByNameAsc());
            return "book-form";
        }
        Category category = categoryRepository.findById(bookForm.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Vui lòng chọn thể loại hợp lệ."));
        bookRepository.save(new Book(bookForm.getTitle().trim(), bookForm.getAuthor().trim(),
                bookForm.getIsbn().trim(), category, bookForm.getAvailableQuantity()));
        redirectAttributes.addFlashAttribute("success", "Đã thêm sách vào thư viện.");
        return "redirect:/books";
    }
}
