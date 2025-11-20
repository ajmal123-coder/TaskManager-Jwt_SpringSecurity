package com.Task.Manager.controller;

import com.Task.Manager.dto.AuthRequest;
import com.Task.Manager.dto.AuthResponse;
import com.Task.Manager.model.User;
import com.Task.Manager.repository.UserRepository;
import com.Task.Manager.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        // Check if user already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(new AuthResponse("Error: Username is already taken!"));
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);

        // Generate token
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(jwtToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // If authentication is successful, find user and generate token
        UserDetails user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(); // Should not happen if authentication passed
        String jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthResponse(jwtToken));
    }
}