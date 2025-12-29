package de.greenflash.taskserviceapi.controller;

import static de.greenflash.taskserviceapi.TestFixtures.TASK_PRIORITY;
import static de.greenflash.taskserviceapi.TestFixtures.USER_A_PASSWORD;
import static de.greenflash.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.greenflash.taskserviceapi.TestcontainersConfiguration;
import de.greenflash.taskserviceapi.DockerAvailableCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@ExtendWith(DockerAvailableCondition.class)
class TaskControllerIntegrationTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void listTasksSupportsPagination() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);
        createTask(token, "Task 1", "First");
        createTask(token, "Task 2", "Second");

        mockMvc.perform(get("/api/task")
                        .param("page", "0")
                        .param("size", "1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void invalidSortFieldReturnsBadRequest() throws Exception {
        String token = login(USER_A_USERNAME, USER_A_PASSWORD);

        mockMvc.perform(get("/api/task")
                        .param("sort", "title,desc")
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

    private void createTask(String token, String title, String description) throws Exception {
        String payload = """
                {
                  "title": "%s",
                  "description": "%s",
                  "priority": "%s"
                }
                """.formatted(title, description, TASK_PRIORITY.name());

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
