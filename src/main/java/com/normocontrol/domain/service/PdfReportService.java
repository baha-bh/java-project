package com.normocontrol.domain.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.normocontrol.domain.model.AnalysisReport;
import com.normocontrol.domain.model.Violation;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    public byte[] generateReport(AnalysisReport report) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);

        document.open();

        // Register system fonts to find Arial or other Cyrillic-supporting fonts
        FontFactory.registerDirectories();

        // Fonts - Supporting Cyrillic
        Font titleFont;
        Font headerFont;
        Font normalFont;
        
        // Try to find a font that supports Cyrillic (Arial is common on Windows)
        String fontName = "Arial";
        if (!FontFactory.isRegistered(fontName)) {
            fontName = FontFactory.HELVETICA; // Fallback
        }

        titleFont = FontFactory.getFont(fontName, "Cp1251", true, 18, Font.BOLD);
        headerFont = FontFactory.getFont(fontName, "Cp1251", true, 12, Font.BOLD);
        normalFont = FontFactory.getFont(fontName, "Cp1251", true, 10, Font.NORMAL);

        // Title
        Paragraph title = new Paragraph("Normocontrol Analysis Report / Отчет нормоконтроля", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Summary
        String projectName = "N/A";
        if (report.getProject() != null) {
            projectName = report.getProject().getName();
        }
        
        document.add(new Paragraph("Project: " + projectName, normalFont));
        document.add(new Paragraph("Date: " + (report.getCreatedAt() != null ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "N/A"), normalFont));
        document.add(new Paragraph("Total Violations: " + (report.getViolations() != null ? report.getViolations().size() : 0), normalFont));
        document.add(new Paragraph(" ", normalFont));

        // Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        try {
            table.setWidths(new float[]{3f, 1f, 2f, 4f});
        } catch (DocumentException e) {
            // fallback
        }

        addTableHeader(table, headerFont);

        if (report.getViolations() != null) {
            for (Violation violation : report.getViolations()) {
                table.addCell(new Phrase(violation.getFilePath() != null ? violation.getFilePath() : "N/A", normalFont));
                table.addCell(new Phrase(String.valueOf(violation.getLineNumber()), normalFont));
                
                String ruleText = "N/A";
                if (violation.getRule() != null) {
                    // Show Name if available, fallback to Code
                    ruleText = violation.getRule().getName() != null ? violation.getRule().getName() : violation.getRule().getCode();
                }
                table.addCell(new Phrase(ruleText != null ? ruleText : "N/A", normalFont));
                table.addCell(new Phrase(violation.getMessage() != null ? violation.getMessage() : "N/A", normalFont));
            }
        }

        document.add(table);
        document.close();

        return out.toByteArray();
    }

    private void addTableHeader(PdfPTable table, Font font) {
        String[] headers = {"Файл", "Строка", "Правило", "Сообщение"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell();
            cell.setPadding(5);
            cell.setPhrase(new Phrase(header, font));
            table.addCell(cell);
        }
    }
}
