package com.normocontrol.domain.service;

import com.normocontrol.domain.model.CheckResult;
import com.normocontrol.domain.port.CheckResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckResultService {

    private final CheckResultRepository checkResultRepository;

    public CheckResult createCheckResult(CheckResult checkResult) {
        return checkResultRepository.save(checkResult);
    }

    public Optional<CheckResult> getCheckResultById(UUID id) {
        return checkResultRepository.findById(id);
    }

    public List<CheckResult> getAllCheckResults() {
        return checkResultRepository.findAll();
    }

    public void deleteCheckResult(UUID id) {
        checkResultRepository.deleteById(id);
    }
}
