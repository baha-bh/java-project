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
            User user = userRepository.findById(userId)
                .orElseGet(() -> userRepository.findByEmail(email)
                    .map(existing -> {
                        // User exists with same email but different ID (e.g. recreated in Supabase)
                        // We delete the old record and create a new one with the current ID
                        userRepository.deleteById(existing.getId());
                        return userRepository.save(User.builder()
                                .id(userId)
                                .email(email)
                                .username(email != null ? email : userId.toString())
                                .role(existing.getRole())
                                .build());
                    })
                    .orElseGet(() -> userRepository.save(User.builder()
                            .id(userId)
                            .email(email)
                            .username(email != null ? email : userId.toString())
                            .role(Role.USER)
                            .build()))
                );
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
