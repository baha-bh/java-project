package com.normocontrol.domain.service;

import com.normocontrol.domain.model.CheckResult;
import com.normocontrol.domain.model.CheckStatus;
import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.Violation;
import com.normocontrol.domain.port.CheckResultRepository;
import com.normocontrol.domain.port.ProjectRepository;
import com.normocontrol.domain.port.RuleRepository;
import com.normocontrol.domain.port.ViolationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ProjectRepository projectRepository;
    private final CheckResultRepository checkResultRepository;
    private final RuleRepository ruleRepository;
    private final ViolationRepository violationRepository;

    @Transactional
    public CheckResult runAnalysis(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // 1. Create CheckResult in progress
        CheckResult checkResult = CheckResult.builder()
                .project(project)
                .status(CheckStatus.IN_PROGRESS)
                .startedAt(OffsetDateTime.now())
                .build();
        
        CheckResult savedCheck = checkResultRepository.save(checkResult);

        // 2. Perform Mock Analysis (In a real system, this would be async)
        // For now, let's just create some dummy violations and finish the check
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .toList();

        if (!activeRules.isEmpty()) {
            // Create a dummy violation for the first rule
            Violation violation = Violation.builder()
                    .checkResult(savedCheck)
                    .rule(activeRules.get(0))
                    .filePath("src/main/java/App.java")
                    .lineNumber(10)
                    .message("Mock violation: Class name should be more descriptive.")
                    .build();
            violationRepository.save(violation);
        }

        // 3. Complete the check
        savedCheck.setStatus(CheckStatus.PASSED);
        savedCheck.setScore(85); // Dummy score
        savedCheck.setCompletedAt(OffsetDateTime.now());

        return checkResultRepository.save(savedCheck);
    }
}
