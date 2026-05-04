package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.AnalysisReport;
import com.normocontrol.domain.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisService analysisService;

    @Test
    @WithMockUser
    void testAnalyzeFile_ShouldReturnOk() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "Test.java",
                "text/plain",
                "public class Test {}".getBytes()
        );

        AnalysisReport mockReport = new AnalysisReport();
        mockReport.setViolations(new ArrayList<>());
        when(analysisService.generateTestReport(any(File.class))).thenReturn(mockReport);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/analysis/test-file")
                .file(file))
                .andExpect(status().isOk());
    }
}
