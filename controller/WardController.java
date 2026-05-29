package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/wards")
@RequiredArgsConstructor
public class WardController {

    private final WardService wardService;
    private final DepartmentService departmentService;

    @GetMapping
    public String wards(Model model) {
        model.addAttribute("wards", wardService.findAll());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("activePage", "wards");
        return "admin/wards";
    }

    @PostMapping("/save")
    public String save(@RequestParam String number,
                       @RequestParam(required = false) Long departmentId,
                       @RequestParam String type,
                       @RequestParam(required = false) Integer capacity,
                       @RequestParam(required = false) Integer occupied,
                       @RequestParam(required = false) String description,
                       RedirectAttributes ra) {
        Ward ward = Ward.builder()
                .number(number)
                .department(departmentId != null ? departmentService.findById(departmentId).orElse(null) : null)
                .type(Ward.WardType.valueOf(type))
                .capacity(capacity)
                .occupied(occupied != null ? occupied : 0)
                .description(description)
                .build();
        wardService.save(ward);
        ra.addFlashAttribute("success", "Палата добавлена");
        return "redirect:/admin/wards";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        wardService.deleteById(id);
        ra.addFlashAttribute("success", "Палата удалена");
        return "redirect:/admin/wards";
    }
}
