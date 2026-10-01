package com.library.management.web;

import com.library.management.dto.*;
import com.library.management.model.*;
import com.library.management.repository.*;
import com.library.management.service.BorrowingService;
import com.library.management.service.FineCalculationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.Arrays;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Controller
public class BorrowingController {
    private final BorrowingService borrowingService;
    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final FinePolicyRepository policyRepository;
    private final FineCalculationService fineCalculationService;

    public BorrowingController(BorrowingService borrowingService, BorrowingRepository borrowingRepository,
                               BookRepository bookRepository, MemberRepository memberRepository,
                               FinePolicyRepository policyRepository, FineCalculationService fineCalculationService) {
        this.borrowingService = borrowingService;
        this.borrowingRepository = borrowingRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.policyRepository = policyRepository;
        this.fineCalculationService = fineCalculationService;
    }

    @GetMapping("/borrowings")
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) Long memberId,
                       @RequestParam(required = false) LocalDate from,
                       @RequestParam(required = false) LocalDate to, Model model) {
        var borrowings = borrowingRepository.findAllByOrderByBorrowedDateDesc();
        if (status != null && !status.isBlank())
            borrowings = borrowings.stream().filter(b -> displayStatus(b) == BorrowingStatus.valueOf(status)).toList();
        if (memberId != null) borrowings = borrowings.stream().filter(b -> b.getMember().getId().equals(memberId)).toList();
        if (from != null) borrowings = borrowings.stream().filter(b -> !b.getBorrowedDate().isBefore(from)).toList();
        if (to != null) borrowings = borrowings.stream().filter(b -> !b.getBorrowedDate().isAfter(to)).toList();
        model.addAttribute("borrowings", borrowings);
        model.addAttribute("statuses", Arrays.asList(BorrowingStatus.values()));
        model.addAttribute("members", memberRepository.findAllByOrderByFullNameAsc());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedMemberId", memberId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        return "borrowings";
    }

    @GetMapping("/borrowings/new")
    public String newBorrowing(Model model) {
        BorrowingForm form = new BorrowingForm();
        form.setDueDate(LocalDate.now().plusDays(14));
        model.addAttribute("borrowingForm", form);
        model.addAttribute("members", memberRepository.findAllByOrderByFullNameAsc());
        model.addAttribute("books", bookRepository.findAllByOrderByTitleAsc());
        return "borrowing-form";
    }

    @PostMapping("/borrowings")
    public String create(@Valid @ModelAttribute("borrowingForm") BorrowingForm form, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("members", memberRepository.findAllByOrderByFullNameAsc());
            model.addAttribute("books", bookRepository.findAllByOrderByTitleAsc());
            return "borrowing-form";
        }
        Borrowing borrowing = borrowingService.create(form.getMemberId(), form.getDueDate(), form.getLines());
        redirectAttributes.addFlashAttribute("success", "Đã tạo phiếu mượn #" + borrowing.getId() + ".");
        return "redirect:/borrowings/" + borrowing.getId();
    }

    @GetMapping("/borrowings/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Borrowing borrowing = borrowingRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));
        model.addAttribute("borrowing", borrowing);
        model.addAttribute("displayStatus", displayStatus(borrowing));
        model.addAttribute("waiverForm", new FineWaiverForm());
        return "borrowing-detail";
    }

    @GetMapping("/borrowings/{id}/return")
    public String returnForm(@PathVariable Long id, Model model) {
        Borrowing borrowing = borrowingRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));
        if (borrowing.isFullyReturned() || borrowing.getStatus() == BorrowingStatus.CANCELLED) {
            throw new IllegalStateException("Phiếu này không còn sách để trả.");
        }
        ReturnForm form = new ReturnForm();
        for (BorrowingDetail detail : borrowing.getDetails()) {
            if (detail.getRemainingQuantity() == 0) continue;
            ReturnLineForm line = new ReturnLineForm();
            line.setDetailId(detail.getId());
            line.setTitle(detail.getBook().getTitle());
            line.setRemainingQuantity(detail.getRemainingQuantity());
            line.setFineAlreadyCharged(detail.getFineAmount());
            form.getLines().add(line);
        }
        FinePolicy policy = policyRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình chính sách phí phạt."));
        model.addAttribute("borrowing", borrowing);
        model.addAttribute("returnForm", form);
        model.addAttribute("policy", policy);
        model.addAttribute("today", LocalDate.now());
        return "return-form";
    }

    @PostMapping("/borrowings/{id}/return")
    public String returnBooks(@PathVariable Long id, @ModelAttribute ReturnForm form,
                              RedirectAttributes redirectAttributes) {
        borrowingService.returnBooks(id, form.getLines());
        redirectAttributes.addFlashAttribute("success", "Đã ghi nhận trả sách.");
        return "redirect:/borrowings/" + id;
    }

    @PostMapping("/borrowings/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        borrowingService.cancel(id);
        redirectAttributes.addFlashAttribute("success", "Đã hủy phiếu mượn và hoàn lại tồn kho.");
        return "redirect:/borrowings/" + id;
    }

    @GetMapping("/borrowings/{id}/payments/new")
    public String newPayment(@PathVariable Long id, Model model) {
        Borrowing borrowing = borrowingRepository.findWithDetailsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn."));
        model.addAttribute("borrowing", borrowing);
        model.addAttribute("paymentForm", new FinePaymentForm());
        return "payment-form";
    }

    @PostMapping("/borrowings/{id}/payments")
    public String pay(@PathVariable Long id, @Valid @ModelAttribute("paymentForm") FinePaymentForm form,
                      BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("borrowing", borrowingRepository.findWithDetailsById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu mượn.")));
            return "payment-form";
        }
        borrowingService.payFine(id, form.getAmount(), form.getMethod(), form.getNote());
        redirectAttributes.addFlashAttribute("success", "Đã ghi nhận thanh toán phí phạt.");
        return "redirect:/borrowings/" + id;
    }

    @PostMapping("/borrowings/{id}/waivers")
    public String waive(@PathVariable Long id, @Valid @ModelAttribute("waiverForm") FineWaiverForm form,
                        BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) throw new IllegalArgumentException(result.getAllErrors().getFirst().getDefaultMessage());
        borrowingService.waiveFine(id, form.getAmount(), form.getReason(), form.getApprovedBy());
        redirectAttributes.addFlashAttribute("success", "Đã ghi nhận miễn giảm phí.");
        return "redirect:/borrowings/" + id;
    }

    @GetMapping("/reports/overdue")
    public String overdue(Model model) {
        FinePolicy policy = policyRepository.findFirstByActiveTrue()
                .orElseThrow(() -> new IllegalStateException("Chưa cấu hình chính sách phí phạt."));
        var overdue = borrowingRepository.findAllByOrderByBorrowedDateDesc().stream()
                .filter(b -> b.getStatus() != BorrowingStatus.CANCELLED && !b.isFullyReturned()
                        && b.getDueDate().isBefore(LocalDate.now()))
                .map(b -> {
                    int lateDays = (int) ChronoUnit.DAYS.between(b.getDueDate(), LocalDate.now());
                    int remaining = b.getDetails().stream().mapToInt(BorrowingDetail::getRemainingQuantity).sum();
                    BigDecimal projected = b.getTotalFineAmount();
                    for (BorrowingDetail detail : b.getDetails()) {
                        if (detail.getRemainingQuantity() > 0) {
                            projected = projected.add(fineCalculationService.calculate(
                                    b.getDueDate(), LocalDate.now(), detail.getRemainingQuantity(),
                                    detail.getFineAmount(), policy).fineAmount());
                        }
                    }
                    return new OverdueRow(b, lateDays, remaining, projected);
                }).toList();
        model.addAttribute("borrowings", overdue);
        model.addAttribute("today", LocalDate.now());
        return "overdue";
    }

    private BorrowingStatus displayStatus(Borrowing borrowing) { return borrowing.getDisplayStatus(); }

    public record OverdueRow(Borrowing borrowing, int lateDays, int remainingQuantity, BigDecimal estimatedFine) {}
}
