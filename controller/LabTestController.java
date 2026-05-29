package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.*;

@Controller
@RequestMapping("/patient/tests")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;
    private final TestOrderService testOrderService;
    private final UserService userService;
    private final PatientService patientService;

    private Patient getPatient(UserDetails ud) {
        return userService.findByUsername(ud.getUsername())
                .flatMap(patientService::findByUser).orElse(null);
    }

    @GetMapping
    public String catalog(@AuthenticationPrincipal UserDetails ud,
                          @RequestParam(required = false) String category,
                          Model model, HttpSession session) {
        Patient patient = getPatient(ud);
        if (patient == null) return "redirect:/login";

        List<LabTest> tests = labTestService.findAvailable();
        if (category != null && !category.isBlank()) {
            tests = tests.stream()
                    .filter(t -> t.getCategory().equalsIgnoreCase(category)).toList();
        }

        @SuppressWarnings("unchecked")
        List<Long> cart = (List<Long>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();

        List<String> categories = labTestService.findAvailable().stream()
                .map(LabTest::getCategory).distinct().sorted().toList();

        model.addAttribute("tests", tests);
        model.addAttribute("cart", cart);
        model.addAttribute("cartCount", cart.size());
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        return "patient/tests";
    }

    @PostMapping("/cart/add/{id}")
    public String addToCart(@PathVariable Long id, HttpSession session,
                            @RequestParam(defaultValue = "") String redirect) {
        @SuppressWarnings("unchecked")
        List<Long> cart = (List<Long>) session.getAttribute("cart");
        if (cart == null) cart = new ArrayList<>();
        if (!cart.contains(id)) cart.add(id);
        session.setAttribute("cart", cart);
        return "redirect:" + (redirect.isBlank() ? "/patient/tests" : redirect);
    }

    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, HttpSession session) {
        @SuppressWarnings("unchecked")
        List<Long> cart = (List<Long>) session.getAttribute("cart");
        if (cart != null) cart.remove(id);
        session.setAttribute("cart", cart);
        return "redirect:/patient/tests/cart";
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<Long> cartIds = (List<Long>) session.getAttribute("cart");
        if (cartIds == null) cartIds = new ArrayList<>();

        List<LabTest> cartTests = cartIds.stream()
                .map(labTestService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();

        java.math.BigDecimal total = cartTests.stream()
                .map(LabTest::getPrice)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        model.addAttribute("cartTests", cartTests);
        model.addAttribute("total", total);
        model.addAttribute("cartCount", cartIds.size());
        return "patient/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(HttpSession session,
                           @AuthenticationPrincipal UserDetails ud,
                           @RequestParam String paymentMethod,
                           RedirectAttributes ra) {
        Patient patient = getPatient(ud);
        if (patient == null) return "redirect:/login";

        @SuppressWarnings("unchecked")
        List<Long> cartIds = (List<Long>) session.getAttribute("cart");
        if (cartIds == null || cartIds.isEmpty()) {
            ra.addFlashAttribute("error", "Корзина пуста");
            return "redirect:/patient/tests/cart";
        }

        TestOrder order = testOrderService.createOrder(patient, cartIds);
        testOrderService.pay(order.getId(), paymentMethod);
        session.setAttribute("cart", new ArrayList<>());
        ra.addFlashAttribute("success", "Оплата прошла успешно! Заказ #" + order.getId() + " оформлен.");
        return "redirect:/patient/tests/orders";
    }

    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal UserDetails ud, Model model, HttpSession session) {
        Patient patient = getPatient(ud);
        if (patient == null) return "redirect:/login";

        @SuppressWarnings("unchecked")
        List<Long> cart = (List<Long>) session.getAttribute("cart");
        model.addAttribute("orders", testOrderService.findByPatient(patient.getId()).stream()
                .sorted((a, b) -> b.getOrderedAt().compareTo(a.getOrderedAt())).toList());
        model.addAttribute("cartCount", cart == null ? 0 : cart.size());
        return "patient/orders";
    }
}
