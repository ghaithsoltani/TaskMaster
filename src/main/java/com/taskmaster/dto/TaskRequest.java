package com.taskmaster.dto;

import com.taskmaster.entity.Task;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO for creating/updating a task.
 * Used as request body in POST/PUT endpoints.
 */
@Data
@Builder
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be less than 2000 characters")
    private String description;

    private Task.Status status;

    @NotNull(message = "Priority is required")
    private Task.Priority priority;

    @Size(max = 50, message = "Assignee must be less than 50 characters")
    private String assignee;

    @FutureOrPresent(message = "Due date must be today or in the future")
    private LocalDate dueDate;
}