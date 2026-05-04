package com.normocontrol.domain.service;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.Violation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
public class DocumentationAnalyzer {

    public List<Violation> analyzeFile(File file, List<Rule> rules) {
        List<Violation> violations = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String fullContent = String.join("\n", lines);

            for (Rule rule : rules) {
                if (!"DOCUMENTATION".equals(rule.getCategory())) continue;

                switch (rule.getCode()) {
                    case "DOC_REQUIRED_SECTION":
                        checkRequiredSection(violations, fullContent, rule);
                        break;
                    case "DOC_NO_FORBIDDEN_WORDS":
                        checkForbiddenWords(violations, lines, rule);
                        break;
                    case "DOC_H1_TITLE":
                        checkH1Title(violations, lines, rule);
                        break;
                    default:
                        // Generic regex check if name or description contains a pattern
                        // For now, we use hardcoded logic for specific codes
                        break;
                }
            }
        } catch (IOException e) {
            log.error("Failed to read file for analysis: {}", file.getAbsolutePath(), e);
        }
        return violations;
    }

    private void checkRequiredSection(List<Violation> violations, String content, Rule rule) {
        // Assume description or name contains the section name to look for
        String sectionName = rule.getName().replace("Наличие раздела ", "");
        if (!content.toLowerCase().contains("# " + sectionName.toLowerCase()) && 
            !content.toLowerCase().contains("## " + sectionName.toLowerCase())) {
            violations.add(Violation.builder()
                    .rule(rule)
                    .message("В документе отсутствует обязательный раздел: " + sectionName)
                    .lineNumber(1)
                    .build());
        }
    }

    private void checkForbiddenWords(List<Violation> violations, List<String> lines, Rule rule) {
        String[] forbidden = {"TODO", "FIXME", "ВНИМАНИЕ", "ЗАМЕТКА"};
        for (int i = 0; i < lines.size(); i++) {
            for (String word : forbidden) {
                if (lines.get(i).contains(word)) {
                    violations.add(Violation.builder()
                            .rule(rule)
                            .message("Документ содержит запрещенное слово: " + word)
                            .lineNumber(i + 1)
                            .build());
                }
            }
        }
    }

    private void checkH1Title(List<Violation> violations, List<String> lines, Rule rule) {
        if (lines.isEmpty() || !lines.get(0).startsWith("# ")) {
            violations.add(Violation.builder()
                    .rule(rule)
                    .message("Документ должен начинаться с заголовка первого уровня (# Название)")
                    .lineNumber(1)
                    .build());
        }
    }
}
