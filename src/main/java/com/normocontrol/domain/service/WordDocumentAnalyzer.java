package com.normocontrol.domain.service;

import com.normocontrol.domain.model.AnalysisReport;
import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.Violation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WordDocumentAnalyzer {

    public List<Violation> analyzeFile(File file, List<Rule> rules) {
        return analyzeReport(file, rules).getViolations();
    }

    public AnalysisReport analyzeReport(File file, List<Rule> rules) {
        List<Violation> violations = new ArrayList<>();
        List<AnalysisReport.CheckDetail> details = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();
            
            // Collect global info
            String fontFound = "Не определен";
            double sizeFound = -1;
            boolean isJustified = true;

            for (XWPFParagraph para : paragraphs) {
                if (para.getText().trim().isEmpty()) continue;
                
                if (!para.getRuns().isEmpty()) {
                    XWPFRun run = para.getRuns().get(0);
                    if (run.getFontFamily() != null) fontFound = run.getFontFamily();
                    Double fontSize = run.getFontSizeAsDouble();
                    if (fontSize != null && fontSize != -1.0) sizeFound = fontSize;
                }
                
                if (para.getAlignment() != null && para.getAlignment().getValue() != 3) isJustified = false;
            }

            // Check against rules and create details
            addCheckDetail(details, "Шрифт (Times New Roman)", fontFound.equalsIgnoreCase("Times New Roman"), fontFound);
            addCheckDetail(details, "Размер шрифта (14pt)", sizeFound == 14.0, sizeFound > 0 ? sizeFound + "pt" : "Не определен");
            addCheckDetail(details, "Выравнивание (По ширине)", isJustified, isJustified ? "Да" : "Нет");

            // Still collect specific violations for the UI
            for (int i = 0; i < paragraphs.size(); i++) {
                XWPFParagraph para = paragraphs.get(i);
                if (para.getText().trim().isEmpty()) continue;
                
                for (Rule rule : rules) {
                    if (!"DOCUMENTATION".equals(rule.getCategory())) continue;
                    checkParagraph(violations, para, rule, i + 1);
                }
            }

        } catch (IOException e) {
            log.error("Failed to analyze Word document: {}", file.getAbsolutePath(), e);
        }

        return AnalysisReport.builder()
                .fileName(file.getName())
                .violations(violations)
                .details(details)
                .score(calculateScore(details))
                .build();
    }

    private void addCheckDetail(List<AnalysisReport.CheckDetail> details, String criteria, boolean passed, String value) {
        details.add(AnalysisReport.CheckDetail.builder()
                .criteria(criteria)
                .passed(passed)
                .foundValue(value)
                .message(passed ? "Соответствует эталону" : "Не соответствует")
                .build());
    }

    private int calculateScore(List<AnalysisReport.CheckDetail> details) {
        if (details.isEmpty()) return 0;
        long passed = details.stream().filter(AnalysisReport.CheckDetail::isPassed).count();
        return (int) ((passed * 100) / details.size());
    }

    private void checkParagraph(List<Violation> violations, XWPFParagraph para, Rule rule, int lineNum) {
        switch (rule.getCode()) {
            case "DOC_FONT_FAMILY":
                if (!para.getRuns().isEmpty()) {
                    String f = para.getRuns().get(0).getFontFamily();
                    if (f != null && !f.equalsIgnoreCase("Times New Roman")) {
                        violations.add(createViolation(rule, "Неверный шрифт: " + f, lineNum));
                    }
                }
                break;
            case "DOC_FONT_SIZE":
                if (!para.getRuns().isEmpty()) {
                    Double s = para.getRuns().get(0).getFontSizeAsDouble();
                    if (s != null && s != -1.0 && s != 14.0) {
                        violations.add(createViolation(rule, "Размер " + s + "пт вместо 14пт", lineNum));
                    }
                }
                break;
        }
    }

    private Violation createViolation(Rule rule, String msg, int line) {
        return Violation.builder().rule(rule).message(msg).lineNumber(line).build();
    }
}
