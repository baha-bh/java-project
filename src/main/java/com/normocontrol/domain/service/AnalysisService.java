package com.normocontrol.domain.service;

import com.normocontrol.domain.model.CheckResult;
import com.normocontrol.domain.model.CheckStatus;
import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.Violation;
import com.normocontrol.domain.model.AnalysisReport;
import com.normocontrol.domain.port.CheckResultRepository;
import com.normocontrol.domain.port.ProjectRepository;
import com.normocontrol.domain.port.RuleRepository;
import com.normocontrol.domain.port.ViolationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ProjectRepository projectRepository;
    private final CheckResultRepository checkResultRepository;
    private final RuleRepository ruleRepository;
    private final ViolationRepository violationRepository;
    private final StaticCodeAnalyzer staticCodeAnalyzer;
    private final DocumentationAnalyzer documentationAnalyzer;
    private final WordDocumentAnalyzer wordDocumentAnalyzer;
    private final GitService gitService;

    @Transactional
    public CheckResult runAnalysis(UUID projectId, String targetPath) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        CheckResult checkResult = CheckResult.builder()
                .project(project)
                .status(CheckStatus.IN_PROGRESS)
                .targetPath(targetPath != null ? targetPath : "/")
                .startedAt(OffsetDateTime.now())
                .build();
        
        return checkResultRepository.save(checkResult);
    }

    @Async
    @Transactional
    public void performAnalysis(UUID checkId) {
        CheckResult checkResult = checkResultRepository.findById(checkId)
                .orElseThrow(() -> new RuntimeException("CheckResult not found"));
        
        Project project = checkResult.getProject();
        File repoDir = null;

        try {
            log.info("Starting async analysis for check {}", checkId);
            
            // 1. Clone repository
            checkResult.setMessage("Клонирование репозитория...");
            checkResultRepository.save(checkResult);
            
            try {
                repoDir = gitService.cloneRepository(project.getRepositoryUrl(), project.getBranch());
            } catch (Exception e) {
                failCheck(checkResult, "Ошибка клонирования: " + e.getMessage());
                return;
            }
            
            // 2. Determine target directory
            checkResult.setMessage("Поиск файлов для анализа...");
            checkResultRepository.save(checkResult);
            
            File analysisDir = repoDir;
            String targetPath = checkResult.getTargetPath();
            if (targetPath != null && !targetPath.equals("/") && !targetPath.isEmpty()) {
                // Normalize path for the OS
                String normalizedPath = targetPath.replace("/", File.separator).replace("\\", File.separator);
                if (normalizedPath.startsWith(File.separator)) {
                    normalizedPath = normalizedPath.substring(1);
                }
                analysisDir = new File(repoDir, normalizedPath);
            }

            if (!analysisDir.exists()) {
                failCheck(checkResult, "Путь не найден в репозитории: " + targetPath);
                return;
            }

            // 3. Perform Analysis
            checkResult.setMessage("Анализ кода и документов (.java, .md, .docx)...");
            checkResultRepository.save(checkResult);

            List<Rule> activeRules = ruleRepository.findAll().stream()
                    .filter(Rule::getIsActive)
                    .toList();

            int filesAnalyzed = analyzeDirectory(analysisDir, checkResult, activeRules);

            if (filesAnalyzed == 0) {
                failCheck(checkResult, "В указанной папке не найдено подходящих файлов (.java, .md, .docx).");
                return;
            }

            // 4. Complete the check
            long violationCount = violationRepository.findAll().stream()
                    .filter(v -> v.getCheckResult().getId().equals(checkId))
                    .count();

            checkResult.setStatus(CheckStatus.PASSED);
            checkResult.setScore(Math.max(0, 100 - (int)violationCount * 5));
            checkResult.setMessage("Анализ завершен. Проверено файлов: " + filesAnalyzed);
            checkResult.setCompletedAt(OffsetDateTime.now());
            
            checkResultRepository.save(checkResult);
            log.info("Analysis completed for check {}. Score: {}", checkId, checkResult.getScore());

        } catch (Exception e) {
            log.error("Analysis failed for check {}", checkId, e);
            failCheck(checkResult, "Внутренняя ошибка: " + e.getMessage());
        } finally {
            if (repoDir != null) {
                gitService.deleteDirectory(repoDir);
            }
        }
    }

    private void failCheck(CheckResult check, String message) {
        check.setStatus(CheckStatus.FAILED);
        check.setMessage(message);
        check.setCompletedAt(OffsetDateTime.now());
        checkResultRepository.save(check);
    }

    private int analyzeDirectory(File dir, CheckResult checkResult, List<Rule> rules) {
        if (!dir.exists() || !dir.isDirectory()) return 0;
        
        File[] files = dir.listFiles();
        if (files == null) return 0;

        int count = 0;
        for (File file : files) {
            if (file.isDirectory()) {
                count += analyzeDirectory(file, checkResult, rules);
            } else {
                String fileName = file.getName().toLowerCase();
                List<Violation> findings = null;
                
                if (fileName.endsWith(".java")) {
                    findings = staticCodeAnalyzer.analyzeFile(file, rules);
                } else if (fileName.endsWith(".md") || fileName.endsWith(".txt")) {
                    findings = documentationAnalyzer.analyzeFile(file, rules);
                } else if (fileName.endsWith(".docx")) {
                    findings = wordDocumentAnalyzer.analyzeFile(file, rules);
                }

                if (findings != null) {
                    for (Violation v : findings) {
                        v.setCheckResult(checkResult);
                        violationRepository.save(v);
                    }
                    count++;
                }
            }
        }
        return count;
    }

    public List<Violation> testAnalyzeFile(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName().toLowerCase();
        
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .toList();

        if (fileName.endsWith(".java")) {
            return staticCodeAnalyzer.analyzeFile(file, activeRules);
        } else if (fileName.endsWith(".md") || fileName.endsWith(".txt")) {
            return documentationAnalyzer.analyzeFile(file, activeRules);
        } else if (fileName.endsWith(".docx")) {
            return wordDocumentAnalyzer.analyzeFile(file, activeRules);
        }
        
        return List.of();
    }

    public List<Violation> analyzeCode(String code) {
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .toList();
        return staticCodeAnalyzer.analyzeCode(code, activeRules);
    }

    public AnalysisReport generateTestReport(File file) {
        String fileName = file.getName().toLowerCase();
        List<Rule> activeRules = ruleRepository.findAll().stream()
                .filter(Rule::getIsActive)
                .toList();

        if (fileName.endsWith(".docx")) {
            return wordDocumentAnalyzer.analyzeReport(file, activeRules);
        }

        List<Violation> violations;
        if (fileName.endsWith(".java")) {
            violations = staticCodeAnalyzer.analyzeFile(file, activeRules);
        } else {
            violations = documentationAnalyzer.analyzeFile(file, activeRules);
        }

        return AnalysisReport.builder()
                .fileName(file.getName())
                .violations(violations)
                .details(List.of())
                .score(Math.max(0, 100 - violations.size() * 10))
                .build();
    }

    public AnalysisReport getReportForCheck(UUID checkId) {
        com.normocontrol.domain.model.CheckResult check = checkResultRepository.findById(checkId)
                .orElseThrow(() -> new RuntimeException("Check not found"));
        
        List<Violation> violations = violationRepository.findAll().stream()
                .filter(v -> v.getCheckResult().getId().equals(checkId))
                .toList();

        return AnalysisReport.builder()
                .fileName("Project Analysis") // In a real app, we'd store the specific filename or project name
                .violations(violations)
                .details(List.of()) // For project checks, we mostly have violations
                .score(check.getScore() != null ? check.getScore() : 0)
                .build();
    }
}
