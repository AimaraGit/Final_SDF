package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToMany
    @JoinTable(name = "test_order_items",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "lab_test_id"))
    private List<LabTest> tests;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalAmount;
    private LocalDateTime orderedAt;
    private LocalDateTime paidAt;
    private String paymentMethod;

    @PrePersist
    protected void onCreate() {
        orderedAt = LocalDateTime.now();
        if (status == null) status = OrderStatus.PENDING;
    }

    public enum OrderStatus {
        PENDING, PAID, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
