package com.library.management.web;

import com.library.management.dto.MemberForm;
import com.library.management.model.Member;
import com.library.management.repository.MemberRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberController {
    private final MemberRepository memberRepository;
    public MemberController(MemberRepository memberRepository) { this.memberRepository = memberRepository; }

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", memberRepository.findAllByOrderByFullNameAsc());
        return "members";
    }

    @GetMapping("/members/new")
    public String newMember(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "member-form";
    }

    @PostMapping("/members")
    public String createMember(@Valid @ModelAttribute MemberForm memberForm, BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) return "member-form";
        memberRepository.save(new Member(memberForm.getFullName().trim(), memberForm.getEmail().trim(),
                memberForm.getPhone() == null ? "" : memberForm.getPhone().trim()));
        redirectAttributes.addFlashAttribute("success", "Đã thêm độc giả.");
        return "redirect:/members";
    }
}
