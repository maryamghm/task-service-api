package de.greenflash.taskserviceapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static de.greenflash.taskserviceapi.TestFixtures.TASK_PRIORITY;
import static de.greenflash.taskserviceapi.TestFixtures.TASK_STATUS;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TaskControllerTest extends AbstractMockMvcIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void rejectsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/task"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanCreateAndFetchOwnTasks() throws Exception {
        String token = login("UserA", "UserA");
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
        String tokenA = login("UserA", "UserA");
        String tokenB = login("UserB", "UserB");
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
        String token = login("UserA", "UserA");
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
