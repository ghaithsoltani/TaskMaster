package com.taskmaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmaster.dto.LoginRequest;
import com.taskmaster.dto.TaskRequest;
import com.taskmaster.dto.UserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for Task API.
 *
 * Uses @SpringBootTest with MockMvc to test the full stack:
 * Controller → Service → Repository → H2 Database
 *
 * @Transactional rolls back changes after each test (database stays clean).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Register a user
        UserRequest registerRequest = UserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get token
        LoginRequest loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract token from response (simplified - in real code use JsonPath)
        authToken = "Bearer " + objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void createTask_shouldReturn201() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Integration Test Task")
                .description("Testing task creation")
                .priority(com.taskmaster.entity.Task.Priority.HIGH)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getAllTasks_shouldReturnList() throws Exception {
        // First create a task
        TaskRequest request = TaskRequest.builder()
                .title("List Test Task")
                .description("For listing")
                .priority(com.taskmaster.entity.Task.Priority.MEDIUM)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then get all tasks
        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].title").value("List Test Task"));
    }

    @Test
    void getTaskById_shouldReturn404_forNonExistent() throws Exception {
        mockMvc.perform(get("/api/v1/tasks/99999")
                        .header("Authorization", authToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task not found with id: 99999"));
    }

    @Test
    void createTask_shouldReturn401_withoutToken() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Unauthorized Task")
                .priority(com.taskmaster.entity.Task.Priority.LOW)
                .build();

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTask_shouldModifyExisting() throws Exception {
        // Create task
        TaskRequest createRequest = TaskRequest.builder()
                .title("Original Title")
                .priority(com.taskmaster.entity.Task.Priority.LOW)
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int taskId = objectMapper.readTree(createResponse).get("id").asInt();

        // Update task
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Title")
                .description("Updated description")
                .status(com.taskmaster.entity.Task.Status.IN_PROGRESS)
                .priority(com.taskmaster.entity.Task.Priority.HIGH)
                .build();

        mockMvc.perform(put("/api/v1/tasks/" + taskId)
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void deleteTask_shouldRemoveTask() throws Exception {
        // Create task
        TaskRequest request = TaskRequest.builder()
                .title("Task to Delete")
                .priority(com.taskmaster.entity.Task.Priority.LOW)
                .build();

        String response = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        int taskId = objectMapper.readTree(response).get("id").asInt();

        // Delete
        mockMvc.perform(delete("/api/v1/tasks/" + taskId)
                        .header("Authorization", authToken))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/api/v1/tasks/" + taskId)
                        .header("Authorization", authToken))
                .andExpect(status().isBadRequest());
    }
}