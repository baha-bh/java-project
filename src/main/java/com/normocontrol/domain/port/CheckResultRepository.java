package com.normocontrol.domain.port;

import com.normocontrol.domain.model.CheckResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CheckResultRepository {
    CheckResult save(CheckResult checkResult);
    Optional<CheckResult> findById(UUID id);
    List<CheckResult> findAll();
    void deleteById(UUID id);
}
