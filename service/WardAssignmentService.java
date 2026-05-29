package com.hospital.service;

import com.hospital.model.*;
import com.hospital.repository.WardAssignmentRepository;
import com.hospital.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WardAssignmentService {

    private final WardAssignmentRepository wardAssignmentRepository;
    private final WardRepository wardRepository;

    @Transactional
    public WardAssignment assign(Patient patient, Ward ward, User assignedBy, String notes) {
        // Discharge from previous ward if exists
        wardAssignmentRepository.findByPatientIdAndStatus(patient.getId(), WardAssignment.Status.ACTIVE)
                .ifPresent(prev -> {
                    prev.setStatus(WardAssignment.Status.DISCHARGED);
                    prev.setDischargedAt(LocalDateTime.now());
                    wardAssignmentRepository.save(prev);
                    Ward prevWard = prev.getWard();
                    if (prevWard.getOccupied() != null && prevWard.getOccupied() > 0) {
                        prevWard.setOccupied(prevWard.getOccupied() - 1);
                        wardRepository.save(prevWard);
                    }
                });

        // Assign to new ward
        ward.setOccupied((ward.getOccupied() != null ? ward.getOccupied() : 0) + 1);
        wardRepository.save(ward);

        return wardAssignmentRepository.save(WardAssignment.builder()
                .patient(patient).ward(ward)
                .assignedBy(assignedBy).notes(notes)
                .status(WardAssignment.Status.ACTIVE).build());
    }

    @Transactional
    public void discharge(Long assignmentId) {
        wardAssignmentRepository.findById(assignmentId).ifPresent(a -> {
            a.setStatus(WardAssignment.Status.DISCHARGED);
            a.setDischargedAt(LocalDateTime.now());
            wardAssignmentRepository.save(a);
            Ward ward = a.getWard();
            if (ward.getOccupied() != null && ward.getOccupied() > 0) {
                ward.setOccupied(ward.getOccupied() - 1);
                wardRepository.save(ward);
            }
        });
    }

    public Optional<WardAssignment> findActiveByPatient(Long patientId) {
        return wardAssignmentRepository.findByPatientIdAndStatus(patientId, WardAssignment.Status.ACTIVE);
    }

    public List<WardAssignment> findByPatient(Long patientId) {
        return wardAssignmentRepository.findByPatientId(patientId);
    }

    public List<WardAssignment> findAllActive() {
        return wardAssignmentRepository.findByStatus(WardAssignment.Status.ACTIVE);
    }
}
