package de.greenflash.taskserviceapi.controller;

import de.greenflash.taskserviceapi.AbstractMockMvcIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static de.greenflash.taskserviceapi.TestFixtures.USER_A_PASSWORD;
import static de.greenflash.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractMockMvcIntegrationTest {

    @Test
    void loginReturnsJwtToken() throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(USER_A_USERNAME, USER_A_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        String payload = """
                {
                  "username": "%s",
                  "password": "wrong"
                }
                """.formatted(USER_A_USERNAME);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());
    }
}
