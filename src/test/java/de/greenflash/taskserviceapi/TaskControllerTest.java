package de.greenflash.taskserviceapi;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void rejectsAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/task"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanCreateAndFetchOwnTasks() throws Exception {
        String token = login("UserA", "UserA");
        String payload = """
                {
                  "title": "Write tests",
                  "description": "Cover task controller",
                  "priority": "HIGH"
                }
                """;

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
                .andExpect(jsonPath("$", hasSize(1)));

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
                  "priority": "MEDIUM"
                }
                """;

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
                  "priority": "LOW"
                }
                """;

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
                  "status": "IN_PROGRESS",
                  "priority": "HIGH"
                }
                """;

        mockMvc.perform(put("/api/task/" + taskId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Final"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

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
