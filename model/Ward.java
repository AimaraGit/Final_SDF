package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "wards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String number;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Enumerated(EnumType.STRING)
    private WardType type;

    private Integer capacity;
    private Integer occupied;
    private String description;

    public enum WardType {
        STANDARD, INTENSIVE_CARE, SURGERY, PRIVATE, CHILDREN
    }

    public int getAvailable() {
        return (capacity != null ? capacity : 0) - (occupied != null ? occupied : 0);
    }
}
