package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ward_assignments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WardAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @ManyToOne
    @JoinColumn(name = "assigned_by_user_id")
    private User assignedBy;

    private LocalDateTime assignedAt;
    private LocalDateTime dischargedAt;
    private String notes;

    @Enumerated(EnumType.STRING)
    private Status status;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
        if (status == null) status = Status.ACTIVE;
    }

    public enum Status {
        ACTIVE, DISCHARGED
    }
}
