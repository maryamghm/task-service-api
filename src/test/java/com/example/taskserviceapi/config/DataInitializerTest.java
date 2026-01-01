package com.example.taskserviceapi.config;

import com.example.taskserviceapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

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
