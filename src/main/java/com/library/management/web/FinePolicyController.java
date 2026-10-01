package com.library.management.web;

import com.library.management.dto.FinePolicyForm;
import com.library.management.repository.FinePolicyRepository;
import com.library.management.service.FinePolicyService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class FinePolicyController {
    private final FinePolicyService policyService;
    private final FinePolicyRepository policyRepository;
    public FinePolicyController(FinePolicyService policyService, FinePolicyRepository policyRepository) {
        this.policyService = policyService;
        this.policyRepository = policyRepository;
    }
    @GetMapping("/fine-policies")
    public String policies(Model model) {
        model.addAttribute("policies", policyService.findAll());
        model.addAttribute("activePolicy", policyRepository.findFirstByActiveTrue().orElse(null));
        model.addAttribute("policyForm", new FinePolicyForm());
        return "fine-policies";
    }
    @PostMapping("/fine-policies")
    public String create(@Valid @ModelAttribute("policyForm") FinePolicyForm form, BindingResult result,
                         Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("policies", policyService.findAll());
            model.addAttribute("activePolicy", policyRepository.findFirstByActiveTrue().orElse(null));
            return "fine-policies";
        }
        policyService.activate(form.getDailyFineAmount(), form.getMaxFineAmount(), form.getGraceDays());
        redirectAttributes.addFlashAttribute("success", "Đã cập nhật chính sách phí phạt.");
        return "redirect:/fine-policies";
    }
}
