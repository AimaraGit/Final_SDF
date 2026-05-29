package com.hospital.service;

import com.hospital.model.LabTest;
import com.hospital.repository.LabTestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LabTestService {
    private final LabTestRepository labTestRepository;
    public LabTest save(LabTest test) { return labTestRepository.save(test); }
    public List<LabTest> findAll() { return labTestRepository.findAll(); }
    public List<LabTest> findAvailable() { return labTestRepository.findByAvailableTrue(); }
    public Optional<LabTest> findById(Long id) { return labTestRepository.findById(id); }
    public void deleteById(Long id) { labTestRepository.deleteById(id); }
}
