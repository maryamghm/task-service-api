package com.example.taskserviceapi.service;

import static com.example.taskserviceapi.TestFixtures.USER_A_USERNAME;
import static com.example.taskserviceapi.TestFixtures.USER_B_USERNAME;
import static com.example.taskserviceapi.TestFixtures.userA;
import static com.example.taskserviceapi.TestFixtures.userB;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.taskserviceapi.entity.User;
import com.example.taskserviceapi.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        User user = userA();
        user.setPassword("hashed");
        when(userRepository.findByUsername(USER_A_USERNAME)).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername(USER_A_USERNAME);

        assertThat(details.getUsername()).isEqualTo(USER_A_USERNAME);
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        verify(userRepository).findByUsername(USER_A_USERNAME);
    }

    @Test
    void findByUsernameReturnsExistingUser() {
        User user = userA();
        user.setPassword("hashed");
        when(userRepository.findByUsername(USER_A_USERNAME)).thenReturn(Optional.of(user));

        User actual = userService.findByUsername(USER_A_USERNAME);
        assertThat(actual).isEqualTo(user);
        verify(userRepository).findByUsername(USER_A_USERNAME);
    }

    @Test
    void findByUsernameThrowsWhenMissing() {
        when(userRepository.findByUsername(USER_A_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername(USER_A_USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
        verify(userRepository).findByUsername(USER_A_USERNAME);
    }

    @Test
    void ensureUsersExistCreatesMissingUsers() {
        User existing = userB();

        when(userRepository.findByUsername(USER_A_USERNAME)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(USER_B_USERNAME)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(USER_A_USERNAME)).thenReturn("encoded");

        userService.ensureUsersExist(List.of(USER_A_USERNAME, USER_B_USERNAME));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(USER_A_USERNAME);
        assertThat(saved.getPassword()).isEqualTo("encoded");

        verify(userRepository, never()).save(existing);
    }
}
