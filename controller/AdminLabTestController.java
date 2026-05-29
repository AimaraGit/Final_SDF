package com.hospital.controller;

import com.hospital.model.LabTest;
import com.hospital.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
@RequestMapping("/admin/lab-tests")
@RequiredArgsConstructor
public class AdminLabTestController {

    private final LabTestService labTestService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tests", labTestService.findAll());
        model.addAttribute("activePage", "lab-tests");
        return "admin/lab-tests";
    }

    @PostMapping("/save")
    public String save(@RequestParam String name,
                       @RequestParam(required = false) String description,
                       @RequestParam String category,
                       @RequestParam(required = false) Integer daysToResult,
                       @RequestParam BigDecimal price,
                       RedirectAttributes ra) {
        LabTest test = LabTest.builder()
                .name(name).description(description).category(category)
                .daysToResult(daysToResult != null ? daysToResult : 1)
                .price(price).available(true).build();
        labTestService.save(test);
        ra.addFlashAttribute("success", "Анализ добавлен");
        return "redirect:/admin/lab-tests";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        labTestService.deleteById(id);
        ra.addFlashAttribute("success", "Анализ удалён");
        return "redirect:/admin/lab-tests";
    }
}
