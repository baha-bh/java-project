package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.AnalysisReport;
import com.normocontrol.domain.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.normocontrol.infrastructure.web.dto.response.ErrorResponse;
import com.normocontrol.domain.service.PdfReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
public class AnalysisController {
    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final AnalysisService analysisService;
    private final PdfReportService pdfReportService;

    @PostMapping("/test-file")
    public ResponseEntity<?> testAnalyzeFile(@RequestParam("file") MultipartFile file) {
        log.info("Received test analysis request for file: {}", file.getOriginalFilename());
        
        Path tempFile = null;
        try {
            // Save to temp file
            tempFile = Files.createTempFile("normo_test_", "_" + file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            
            AnalysisReport report = analysisService.generateTestReport(tempFile.toFile());
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Error during file analysis: {}", e.getMessage(), e);
            java.util.Map<String, Object> error = new java.util.HashMap<>();
            error.put("status", 500);
            error.put("error", "Internal Server Error");
            error.put("message", "Ошибка при анализе файла: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    log.warn("Failed to delete temp file: {}", tempFile);
                }
            }
        }
    }

    @PostMapping("/test-file/download")
    public ResponseEntity<?> downloadTestReport(@RequestParam("file") MultipartFile file) {
        File tempFile = null;
        try {
            tempFile = saveToTemp(file);
            AnalysisReport report = analysisService.generateTestReport(tempFile);
            byte[] pdf = pdfReportService.generateReport(report);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            log.error("Error generating PDF report: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ErrorResponse.builder()
                    .status(500)
                    .error("Error generating PDF")
                    .message(e.getMessage())
                    .build());
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @GetMapping("/check/{id}/download")
    public ResponseEntity<?> downloadCheckReport(@PathVariable("id") java.util.UUID id) {
        try {
            AnalysisReport report = analysisService.getReportForCheck(id);
            byte[] pdf = pdfReportService.generateReport(report);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + id.toString().substring(0, 8) + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ErrorResponse.builder()
                    .status(500)
                    .error("Error generating PDF")
                    .message(e.getMessage())
                    .build());
        }
    }

    private File saveToTemp(MultipartFile file) throws IOException {
        Path tempFile = Files.createTempFile("normo_pdf_", "_" + file.getOriginalFilename());
        file.transferTo(tempFile.toFile());
        return tempFile.toFile();
    }
}
