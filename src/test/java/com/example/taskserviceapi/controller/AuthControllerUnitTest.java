package com.example.taskserviceapi.controller;

import static com.example.taskserviceapi.TestFixtures.USER_A_PASSWORD;
import static com.example.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.taskserviceapi.AbstractMockMvcUnitTest;
import com.example.taskserviceapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest extends AbstractMockMvcUnitTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = buildStandaloneMockMvc(new AuthController(authenticationManager, jwtService));
    }

    @Test
    void loginReturnsToken() throws Exception {
        Authentication authentication = new UsernamePasswordAuthenticationToken(USER_A_USERNAME, "N/A");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken(USER_A_USERNAME)).thenReturn("token-123");

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
                .andExpect(jsonPath("$.token").value("token-123"));
    }

    @Test
    void loginRejectsBlankUsername() throws Exception {
        String payload = """
                {
                  "username": " ",
                  "password": "%s"
                }
                """.formatted(USER_A_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));

        verifyNoInteractions(authenticationManager, jwtService);
    }
}
