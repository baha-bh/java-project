package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.Violation;
import com.normocontrol.domain.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/test-file")
    public ResponseEntity<List<Violation>> testAnalyzeFile(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Received test analysis request for file: {}, size: {} bytes", file.getOriginalFilename(), file.getSize());
        String code = new String(file.getBytes(), StandardCharsets.UTF_8);
        List<Violation> violations = analysisService.analyzeCode(code);
        log.info("Analysis complete. Found {} violations", violations.size());
        return ResponseEntity.ok(violations);
    }
}
