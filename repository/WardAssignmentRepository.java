package com.hospital.repository;

import com.hospital.model.WardAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WardAssignmentRepository extends JpaRepository<WardAssignment, Long> {
    List<WardAssignment> findByPatientId(Long patientId);
    List<WardAssignment> findByWardId(Long wardId);
    List<WardAssignment> findByStatus(WardAssignment.Status status);
    Optional<WardAssignment> findByPatientIdAndStatus(Long patientId, WardAssignment.Status status);
    boolean existsByPatientIdAndStatus(Long patientId, WardAssignment.Status status);
}
