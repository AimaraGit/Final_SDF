package com.hospital.repository;

import com.hospital.model.TestOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestOrderRepository extends JpaRepository<TestOrder, Long> {
    List<TestOrder> findByPatientId(Long patientId);
    List<TestOrder> findByStatus(TestOrder.OrderStatus status);
}
