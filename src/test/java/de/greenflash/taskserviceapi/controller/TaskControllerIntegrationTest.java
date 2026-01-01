package de.greenflash.taskserviceapi.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.greenflash.taskserviceapi.AbstractMockMvcIntegrationTest;
import de.greenflash.taskserviceapi.entity.TaskPriority;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static de.greenflash.taskserviceapi.TestFixtures.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

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
