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

import java.io.File;
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
    private final StaticCodeAnalyzer staticCodeAnalyzer;

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

        // 2. Perform Real Analysis on local source files (POC)
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .toList();

        // For POC, we scan our own src directory
        File srcDir = new File("src/main/java");
        analyzeDirectory(srcDir, savedCheck, activeRules);

        // 3. Complete the check
        long violationCount = violationRepository.findAll().stream()
                .filter(v -> v.getCheckResult().getId().equals(savedCheck.getId()))
                .count();

        savedCheck.setStatus(CheckStatus.PASSED);
        savedCheck.setScore(Math.max(0, 100 - (int)violationCount * 5));
        savedCheck.setCompletedAt(OffsetDateTime.now());

        return checkResultRepository.save(savedCheck);
    }

    private void analyzeDirectory(File dir, CheckResult checkResult, List<Rule> rules) {
        if (!dir.exists() || !dir.isDirectory()) return;
        
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                analyzeDirectory(file, checkResult, rules);
            } else if (file.getName().endsWith(".java")) {
                List<Violation> findings = staticCodeAnalyzer.analyzeFile(file, rules);
                for (Violation v : findings) {
                    v.setCheckResult(checkResult);
                    violationRepository.save(v);
                }
            }
        }
    }

    public List<Violation> testAnalyzeFile(String filePath) {
        File file = new File(filePath);
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .toList();
        return staticCodeAnalyzer.analyzeFile(file, activeRules);
    }

    public List<Violation> analyzeCode(String code) {
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .toList();
        return staticCodeAnalyzer.analyzeCode(code, activeRules);
    }
}
