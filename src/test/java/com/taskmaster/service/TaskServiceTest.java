package com.taskmaster.service;

import com.taskmaster.dto.TaskRequest;
import com.taskmaster.dto.TaskResponse;
import com.taskmaster.entity.Task;
import com.taskmaster.entity.User;
import com.taskmaster.repository.TaskRepository;
import com.taskmaster.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TaskService using Mockito.
 *
 * We mock repositories and security context to isolate the service.
 * This makes tests fast (no database, no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");


        // Setup test user
        testUser = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashed")
                .role(User.Role.USER)
                .build();

        // Setup test task
        testTask = Task.builder()
                .id(1)
                .user(testUser)
                .title("Test Task")
                .description("Test Description")
                .status(Task.Status.TODO)
                .priority(Task.Priority.HIGH)
                .assignee("testuser")
                .dueDate(LocalDate.now().plusDays(7))
                .build();
    }

    @Test
    void getAllTasks_shouldReturnUserTasks() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findByUserId(1)).thenReturn(Collections.singletonList(testTask));

        // When
        var tasks = taskService.getAllTasks();

        // Then
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
        verify(taskRepository).findByUserId(1);
    }

    @Test
    void createTask_shouldSaveAndReturnTask() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskRequest request = TaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .priority(Task.Priority.MEDIUM)
                .build();

        // When
        TaskResponse response = taskService.createTask(request);

        // Then
        assertNotNull(response);
        assertEquals("Test Task", response.getTitle()); // Returns saved task
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTaskById_shouldThrow_whenTaskNotFound() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(999)).thenReturn(Optional.empty());

        // When/Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskService.getTaskById(999)
        );
        assertEquals("Task not found with id: 999", exception.getMessage());
    }

    @Test
    void getTaskById_shouldThrow_whenNotOwner() {
        // Given
        User otherUser = User.builder().id(2).username("other").build();
        Task otherTask = Task.builder().id(2).user(otherUser).build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(2)).thenReturn(Optional.of(otherTask));

        // When/Then
        assertThrows(SecurityException.class, () -> taskService.getTaskById(2));
    }

    @Test
    void deleteTask_shouldDelete_whenOwner() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));

        // When
        taskService.deleteTask(1);

        // Then
        verify(taskRepository).delete(testTask);
    }
}