package com.example.taskserviceapi.config;

import static org.mockito.Mockito.verify;

import com.example.taskserviceapi.service.UserService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    void initUsersSeedsDefaultUsers() throws Exception {
        dataInitializer.initUsers(userService).run();

        verify(userService).ensureUsersExist(List.of("UserA", "UserB"));
    }
}
