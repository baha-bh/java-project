package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.AnalysisReport;
import com.normocontrol.domain.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisService analysisService;

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
}
