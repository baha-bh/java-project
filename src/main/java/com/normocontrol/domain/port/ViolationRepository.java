package com.normocontrol.domain.port;

import com.normocontrol.domain.model.Violation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViolationRepository {
    Violation save(Violation violation);
    Optional<Violation> findById(UUID id);
    List<Violation> findAll();
    void deleteById(UUID id);
}
