package de.greenflash.taskserviceapi.controller;

import de.greenflash.taskserviceapi.dto.AuthRequest;
import de.greenflash.taskserviceapi.dto.AuthResponse;
import de.greenflash.taskserviceapi.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // Handles authentication and token issuance.
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Authenticate credentials and return a JWT.
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        String token = jwtService.generateToken(authentication.getName());
        return new AuthResponse(token);
    }
}
