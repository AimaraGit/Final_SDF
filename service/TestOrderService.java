package com.hospital.service;

import com.hospital.model.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestOrderService {

    private final TestOrderRepository testOrderRepository;
    private final LabTestRepository labTestRepository;

    @Transactional
    public TestOrder createOrder(Patient patient, List<Long> testIds) {
        List<LabTest> tests = labTestRepository.findAllById(testIds);
        BigDecimal total = tests.stream()
                .map(LabTest::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        TestOrder order = TestOrder.builder()
                .patient(patient)
                .tests(tests)
                .totalAmount(total)
                .status(TestOrder.OrderStatus.PENDING)
                .build();
        return testOrderRepository.save(order);
    }

    @Transactional
    public TestOrder pay(Long orderId, String paymentMethod) {
        TestOrder order = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(TestOrder.OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);
        return testOrderRepository.save(order);
    }

    public List<TestOrder> findByPatient(Long patientId) {
        return testOrderRepository.findByPatientId(patientId);
    }

    public Optional<TestOrder> findById(Long id) { return testOrderRepository.findById(id); }

    public List<TestOrder> findAll() { return testOrderRepository.findAll(); }

    public void deleteById(Long id) { testOrderRepository.deleteById(id); }

    @Transactional
    public TestOrder createOrderByDoctor(Patient patient, List<Long> testIds) {
        List<LabTest> tests = labTestRepository.findAllById(testIds);
        BigDecimal total = tests.stream()
                .map(LabTest::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        TestOrder order = TestOrder.builder()
                .patient(patient).tests(tests)
                .totalAmount(total)
                .status(TestOrder.OrderStatus.PAID) // Doctor-assigned = auto-paid by hospital
                .paidAt(java.time.LocalDateTime.now())
                .paymentMethod("DOCTOR_ASSIGNED")
                .build();
        return testOrderRepository.save(order);
    }
}
