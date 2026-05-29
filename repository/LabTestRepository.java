package com.hospital.repository;

import com.hospital.model.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByAvailableTrue();
    List<LabTest> findByCategoryIgnoreCase(String category);
}
