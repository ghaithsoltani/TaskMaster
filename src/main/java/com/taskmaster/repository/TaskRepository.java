package com.taskmaster.repository;

import com.taskmaster.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Task entity.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    /**
     * Find all tasks for a specific user.
     */
    List<Task> findByUserId(Integer userId);

    /**
     * Find tasks by status.
     */
    List<Task> findByStatus(Task.Status status);

    /**
     * Find tasks by priority.
     */
    List<Task> findByPriority(Task.Priority priority);

    /**
     * Find tasks by assignee.
     */
    List<Task> findByAssignee(String assignee);

    /**
     * Search tasks by title (case-insensitive, partial match).
     * Uses JPQL (Java Persistence Query Language).
     */
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Task> searchByTitle(@Param("search") String search);

    /**
     * Find tasks by user and status.
     */
    List<Task> findByUserIdAndStatus(Integer userId, Task.Status status);

    /**
     * Count tasks by status for a user.
     */
    long countByUserIdAndStatus(Integer userId, Task.Status status);
}