package com.normocontrol.domain.service;

import com.normocontrol.domain.model.Violation;
import com.normocontrol.domain.port.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViolationService {

    private final ViolationRepository violationRepository;

    public Violation createViolation(Violation violation) {
        return violationRepository.save(violation);
    }

    public Optional<Violation> getViolationById(UUID id) {
        return violationRepository.findById(id);
    }

    public List<Violation> getAllViolations() {
        return violationRepository.findAll();
    }

    public void deleteViolation(UUID id) {
        violationRepository.deleteById(id);
    }
}
