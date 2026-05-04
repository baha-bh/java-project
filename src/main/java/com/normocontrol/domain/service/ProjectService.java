package com.normocontrol.domain.service;

import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.model.User;
import com.normocontrol.domain.port.ProjectRepository;
import com.normocontrol.domain.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
 
    @Transactional
    public Project createProject(Project project, UUID ownerId, String email) {
        if (ownerId != null) {
            User owner = userRepository.findById(ownerId)
                .orElseGet(() -> userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in local database by ID or Email")));
            project.setOwner(owner);
        }
        return projectRepository.save(project);
    }

    public Optional<Project> getProjectById(UUID id) {
        return projectRepository.findById(id);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public void deleteProject(UUID id) {
        projectRepository.deleteById(id);
    }
}
