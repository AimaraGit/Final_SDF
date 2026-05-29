package com.hospital.repository;

import com.hospital.model.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByDepartmentId(Long departmentId);
}
