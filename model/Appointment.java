package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false)
    private LocalDateTime appointmentTime;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String reason;
    private String notes;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = Status.SCHEDULED;
    }

    public enum Status {
        SCHEDULED,   // Запланирован
        ACCEPTED,    // Принят (врач подтвердил)
        IN_PROGRESS, // На приёме
        COMPLETED,   // Завершён
        CANCELLED,   // Отменён
        NO_SHOW      // Не явился
    }
}
