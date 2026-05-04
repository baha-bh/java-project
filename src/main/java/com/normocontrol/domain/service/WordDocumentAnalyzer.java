package com.normocontrol.domain.service;

import com.normocontrol.domain.model.AnalysisReport;
import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.Violation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class WordDocumentAnalyzer {

    public AnalysisReport analyzeReport(File file, List<Rule> activeRules) {
        List<Violation> violations = analyzeFile(file, activeRules);
        return AnalysisReport.builder()
                .fileName(file.getName())
                .violations(violations)
                .score(Math.max(0, 100 - violations.size() * 10))
                .build();
    }

    public List<Violation> analyzeFile(File file, List<Rule> activeRules) {
        List<Violation> violations = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            for (int i = 0; i < document.getParagraphs().size(); i++) {
                XWPFParagraph paragraph = document.getParagraphs().get(i);
                checkParagraph(paragraph, i + 1, file.getName(), activeRules, violations);
            }

        } catch (Exception e) {
            log.error("Error analyzing Word file: {}", e.getMessage());
        }
        return violations;
    }

    private void checkParagraph(XWPFParagraph paragraph, int lineNum, String fileName, List<Rule> activeRules, List<Violation> violations) {
        for (Rule rule : activeRules) {
            if ("GOST_FONT_SIZE".equals(rule.getCode())) {
                for (XWPFRun run : paragraph.getRuns()) {
                    int size = run.getFontSize();
                    if (size != -1 && size != 14) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(lineNum)
                                .message("Нарушение ГОСТ: Размер шрифта должен быть 14pt (обнаружено " + size + "pt).")
                                .build());
                        break; // One violation per paragraph for font
                    }
                }
            } else if ("GOST_FONT_FAMILY".equals(rule.getCode())) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String font = run.getFontFamily();
                    if (font != null && !font.contains("Times New Roman")) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(lineNum)
                                .message("Нарушение ГОСТ: Шрифт должен быть Times New Roman (обнаружено " + font + ").")
                                .build());
                        break;
                    }
                }
            } else if ("GOST_LINE_SPACING".equals(rule.getCode())) {
                // Check for 1.5 line spacing (standard is approx 360-400 twips)
                double spacing = paragraph.getSpacingBetween();
                if (spacing != -1 && spacing < 1.5) {
                    violations.add(Violation.builder()
                            .rule(rule)
                            .filePath(fileName)
                            .lineNumber(lineNum)
                            .message("Нарушение ГОСТ: Межстрочный интервал должен быть не менее 1.5.")
                            .build());
                }
            }
        }
    }
}
