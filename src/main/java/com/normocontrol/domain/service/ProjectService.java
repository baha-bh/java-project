package com.normocontrol.domain.service;

import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.model.Role;
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
    public Project createProject(Project project, UUID userId, String email) {
        if (userId != null) {
            User user = userRepository.findById(userId).orElseGet(() -> {
                User newUser = User.builder()
                        .id(userId)
                        .email(email)
                        .username(email != null ? email : userId.toString())
                        .role(Role.USER)
                        .build();
                return userRepository.save(newUser);
            });
            project.setOwner(user);
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
