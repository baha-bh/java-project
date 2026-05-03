package com.normocontrol.infrastructure.web.controller;

import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.model.User;
import com.normocontrol.domain.service.ProjectService;
import com.normocontrol.infrastructure.web.dto.request.ProjectRequest;
import com.normocontrol.infrastructure.web.dto.response.ProjectResponse;
import com.normocontrol.infrastructure.web.mapper.WebProjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AnalysisService analysisService;
    private final WebProjectMapper mapper;

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ProjectResponse> analyzeProject(
            @PathVariable UUID id) {
        analysisService.runAnalysis(id);
        return projectService.getProjectById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        
        Project project = mapper.toDomain(request);
        
        // Extract user from JWT (Supabase Auth)
        if (jwt != null && jwt.getSubject() != null) {
            project.setOwner(User.builder().id(UUID.fromString(jwt.getSubject())).build());
        }

        Project created = projectService.createProject(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> projects = projectService.getAllProjects().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        return projectService.getProjectById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
