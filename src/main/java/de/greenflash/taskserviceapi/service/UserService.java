package de.greenflash.taskserviceapi.service;

import de.greenflash.taskserviceapi.entity.User;
import de.greenflash.taskserviceapi.repository.UserRepository;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

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
