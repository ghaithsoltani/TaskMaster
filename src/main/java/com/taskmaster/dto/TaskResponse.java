package com.taskmaster.dto;

import com.taskmaster.entity.Task;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for returning task data in API responses.
 * Excludes internal fields like user_id (exposed via assignee instead).
 */
@Data
@Builder
public class TaskResponse {

    private Integer id;
    private String title;
    private String description;
    private Task.Status status;
    private Task.Priority priority;
    private String assignee;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}