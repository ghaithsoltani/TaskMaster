package com.taskmaster.controller;

import com.taskmaster.dto.TaskRequest;
import com.taskmaster.dto.TaskResponse;
import com.taskmaster.entity.Task;
import com.taskmaster.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Task Controller.
 * RESTful API for task management.
 *
 * Base path: /api/v1/tasks (from application.yml context-path + @RequestMapping)
 */
@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        log.debug("GET /tasks - fetching all tasks");
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Integer id) {
        log.debug("GET /tasks/{} - fetching task", id);
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        log.info("POST /tasks - creating task: {}", request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Integer id,
            @Valid @RequestBody TaskRequest request) {
        log.info("PUT /tasks/{} - updating task", id);
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        log.info("DELETE /tasks/{} - deleting task", id);
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<TaskResponse>> searchTasks(@RequestParam String q) {
        log.debug("GET /tasks/search?q={} - searching tasks", q);
        return ResponseEntity.ok(taskService.searchTasks(q));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable Task.Status status) {
        log.debug("GET /tasks/status/{} - filtering by status", status);
        return ResponseEntity.ok(taskService.getTasksByStatus(status));
    }
}