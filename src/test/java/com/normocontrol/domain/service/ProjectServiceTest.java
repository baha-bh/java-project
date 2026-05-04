package com.normocontrol.domain.service;

import com.normocontrol.domain.model.Project;
import com.normocontrol.domain.model.User;
import com.normocontrol.domain.port.ProjectRepository;
import com.normocontrol.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectService projectService;

    private User testUser;
    private Project testProject;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("test@example.com");

        testProject = new Project();
        testProject.setName("Test Project");
    }

    @Test
    void createProject_WhenUserExists_ShouldSaveProjectWithUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Act
        Project result = projectService.createProject(testProject, userId, testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testUser, testProject.getOwner());
        verify(projectRepository).save(testProject);
    }

    @Test
    void createProject_WhenUserNotFoundByIdButFoundByEmail_ShouldSaveProjectWithUser() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        // Act
        Project result = projectService.createProject(testProject, userId, testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(testUser, testProject.getOwner());
        verify(userRepository).findByEmail(testUser.getEmail());
        verify(projectRepository).save(testProject);
    }

    @Test
    void createProject_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            projectService.createProject(testProject, userId, testUser.getEmail())
        );
    }

    @Test
    void getProjectById_ShouldReturnProject() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));

        // Act
        Optional<Project> result = projectService.getProjectById(projectId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProject, result.get());
    }
}
