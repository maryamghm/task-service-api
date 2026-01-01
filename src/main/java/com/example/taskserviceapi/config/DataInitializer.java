package com.example.taskserviceapi.config;

import com.example.taskserviceapi.service.UserService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    /**
     * Seed the required demo users on startup.
     */
    @Bean
    public CommandLineRunner initUsers(UserService userService) {
        return args -> userService.ensureUsersExist(List.of("UserA", "UserB"));
    }
}
