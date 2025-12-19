package de.greenflash.taskserviceapi.config;

import de.greenflash.taskserviceapi.service.UserService;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserService userService) {
        return args -> userService.ensureUsersExist(List.of("UserA", "UserB"));
    }
}
