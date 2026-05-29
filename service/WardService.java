package com.hospital.service;

import com.hospital.model.Ward;
import com.hospital.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WardService {
    private final WardRepository wardRepository;
    public Ward save(Ward ward) { return wardRepository.save(ward); }
    public List<Ward> findAll() { return wardRepository.findAll(); }
    public Optional<Ward> findById(Long id) { return wardRepository.findById(id); }
    public void deleteById(Long id) { wardRepository.deleteById(id); }
    public long count() { return wardRepository.count(); }
}
