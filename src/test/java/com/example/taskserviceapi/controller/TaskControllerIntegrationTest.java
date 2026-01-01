package com.example.taskserviceapi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.taskserviceapi.AbstractMockMvcIntegrationTest;
import com.example.taskserviceapi.entity.TaskPriority;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static com.example.taskserviceapi.TestFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void rejectsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/task"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanCreateAndFetchOwnTasks() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);
        String payload = """
                {
                  "title": "Write tests",
                  "description": "Cover task controller",
                  "priority": "%s"
                }
                """.formatted(TASK_PRIORITY.name());

        MvcResult createResult = mockMvc.perform(post("/api/task")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long taskId = created.get("id").asLong();

        mockMvc.perform(get("/api/task")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));

        mockMvc.perform(get("/api/task/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId));
    }

    @Test
    void userCannotAccessOthersTasks() throws Exception {
        String tokenA = login(USER_A_USERNAME, USER_A_PASSWORD);
        String tokenB = login(USER_B_USERNAME, USER_B_PASSWORD);
        String payload = """
                {
                  "title": "UserA Task",
                  "description": "Private",
                  "priority": "%s"
                }
                """.formatted(TASK_PRIORITY.name());

        MvcResult createResult = mockMvc.perform(post("/api/task")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long taskId = created.get("id").asLong();

        mockMvc.perform(get("/api/task/" + taskId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCanUpdateAndDeleteOwnTask() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);
        String createPayload = """
                {
                  "title": "Draft",
                  "description": "Initial",
                  "priority": "%s"
                }
                """.formatted(TASK_PRIORITY.name());

        MvcResult createResult = mockMvc.perform(post("/api/task")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andReturn();

        long taskId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        String updatePayload = """
                {
                  "title": "Final",
                  "description": "Updated",
                  "status": "%s",
                  "priority": "%s"
                }
                """.formatted(TASK_STATUS.name(), TASK_PRIORITY.name());

        mockMvc.perform(put("/api/task/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Final"))
                .andExpect(jsonPath("$.status").value(TASK_STATUS.name()));

        mockMvc.perform(delete("/api/task/" + taskId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void listTasksSupportsPagination() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);
        createTask(token, "Task 1", "First", TASK_PRIORITY);
        createTask(token, "Task 2", "Second", TASK_PRIORITY);

        mockMvc.perform(get("/api/task")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void listTasksSupportsOrdering() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);
        createTask(token, "Task 1", "First", TaskPriority.HIGH);
        createTask(token, "Task 2", "Second", TaskPriority.LOW);

        mockMvc.perform(get("/api/task")
                        .param("sortProperty", "priority")
                        .param("sortDir", "asc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Task 2"))
                .andExpect(jsonPath("$.content[1].title").value("Task 1"));
    }

    @Test
    void invalidSortFieldReturnsBadRequest() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);

        mockMvc.perform(get("/api/task")
                        .param("sortProperty", "title")
                        .param("sortDir", "desc")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Sorting is only supported by createdAt and priority"));
    }

    @Test
    void createTaskValidationFailsForBlankTitle() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);
        String payload = """
                {
                  "title": " ",
                  "description": "Invalid",
                  "priority": "%s"
                }
                """.formatted(TASK_PRIORITY.name());

        mockMvc.perform(post("/api/task")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    private void createTask(String token, String title, String description, TaskPriority priority) throws Exception {
        String payload = """
                {
                  "title": "%s",
                  "description": "%s",
                  "priority": "%s"
                }
                """.formatted(title, description, priority.name());

        mockMvc.perform(post("/api/task")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());
    }

    private String login(String username, String password) throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }
}
