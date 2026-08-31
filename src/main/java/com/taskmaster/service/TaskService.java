package com.taskmaster.service;

import com.taskmaster.dto.TaskRequest;
import com.taskmaster.dto.TaskResponse;
import com.taskmaster.entity.Task;
import com.taskmaster.entity.User;
import com.taskmaster.repository.TaskRepository;
import com.taskmaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        User currentUser = getCurrentUser();
        log.debug("Fetching tasks for user: {}", currentUser.getUsername());

        return taskRepository.findByUserId(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Integer id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));

        // Verify ownership
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new SecurityException("You don't have permission to view this task");
        }

        return mapToResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        User currentUser = getCurrentUser();

        Task task = Task.builder()
                .user(currentUser)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : Task.Status.TODO)
                .priority(request.getPriority())
                .assignee(request.getAssignee() != null ? request.getAssignee() : currentUser.getUsername())
                .dueDate(request.getDueDate())
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created: {} by user: {}", saved.getId(), currentUser.getUsername());

        return mapToResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Integer id, TaskRequest request) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));

        // Verify ownership
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new SecurityException("You don't have permission to update this task");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setAssignee(request.getAssignee());
        task.setDueDate(request.getDueDate());

        Task updated = taskRepository.save(task);
        log.info("Task updated: {} by user: {}", updated.getId(), currentUser.getUsername());

        return mapToResponse(updated);
    }

    @Transactional
    public void deleteTask(Integer id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));

        // Verify ownership
        if (!task.getUser().getId().equals(currentUser.getId()) && !isAdmin()) {
            throw new SecurityException("You don't have permission to delete this task");
        }

        taskRepository.delete(task);
        log.info("Task deleted: {} by user: {}", id, currentUser.getUsername());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(String search) {
        User currentUser = getCurrentUser();
        return taskRepository.searchByTitle(search).stream()
                .filter(t -> t.getUser().getId().equals(currentUser.getId()) || isAdmin())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByStatus(Task.Status status) {
        User currentUser = getCurrentUser();
        return taskRepository.findByUserIdAndStatus(currentUser.getId(), status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    private boolean isAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assignee(task.getAssignee())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}