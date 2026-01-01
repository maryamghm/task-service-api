package com.example.taskserviceapi.service;

import com.example.taskserviceapi.entity.User;
import com.example.taskserviceapi.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    // Loads users from the database and provides UserDetails for Spring Security.
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Hook for Spring Security authentication.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = findByUsername(username);
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
    }

    /**
     * Resolve a user from persistence or fail with UsernameNotFoundException.
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Ensure the required initial users exist with passwords matching their usernames.
     */
    public void ensureUsersExist(List<String> usernames) {
        for (String username : usernames) {
            userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User user = new User();
                        user.setUsername(username);
                        user.setPassword(passwordEncoder.encode(username));
                        return userRepository.save(user);
                    });
        }
    }
}
