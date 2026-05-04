package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.CheckResult;
import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.service.AnalysisService;
import com.normocontrol.domain.service.ProjectService;
import com.normocontrol.infrastructure.web.dto.request.AnalysisRequest;
import com.normocontrol.infrastructure.web.dto.request.ProjectRequest;
import com.normocontrol.infrastructure.web.dto.response.CheckResultResponse;
import com.normocontrol.infrastructure.web.dto.response.ProjectResponse;
import com.normocontrol.infrastructure.web.mapper.WebCheckResultMapper;
import com.normocontrol.infrastructure.web.mapper.WebProjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AnalysisService analysisService;
    private final WebProjectMapper projectMapper;
    private final WebCheckResultMapper checkMapper;

    @PostMapping("/{id}/analyze")
    public ResponseEntity<CheckResultResponse> analyzeProject(
            @PathVariable UUID id,
            @RequestBody(required = false) AnalysisRequest request) {
        
        String targetPath = request != null ? request.getTargetPath() : "/";
        CheckResult result = analysisService.runAnalysis(id, targetPath);
        
        // Trigger actual analysis in background
        analysisService.performAnalysis(result.getId());
        
        return ResponseEntity.ok(checkMapper.toResponse(result));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        Project project = projectMapper.toDomain(request);
        
        UUID userId = null;
        String email = null;
        if (jwt != null && jwt.getSubject() != null) {
            userId = UUID.fromString(jwt.getSubject());
            email = jwt.getClaimAsString("email");
        }
 
        Project created = projectService.createProject(project, userId, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectMapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects().stream()
                .map(projectMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id)
                .map(projectMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
