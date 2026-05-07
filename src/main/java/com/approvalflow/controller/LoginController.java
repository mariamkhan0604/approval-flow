package com.approvalflow.controller;

import com.approvalflow.model.User;
import com.approvalflow.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and authentication endpoints")
public class LoginController {

    private final UserRepository userRepository;

    /**
     * POST /auth/login
     *
     * Example request body:
     * {
     *   "username": "alice",
     *   "password": "alice123"
     * }
     */
    @PostMapping("/login")
    @Operation(summary = "Login with username and password")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest body) {

        String username = body.getUsername();
        String password = body.getPassword();

        // Find user by username
        User user = userRepository.findByUsername(username).orElse(null);

        // Plain text password comparison
        if (user == null || !password.equals(user.getPassword())) {

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Invalid username or password");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // Success response
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", user.getUserId());
        response.put("name", user.getName());
        response.put("username", user.getUsername());
        response.put("role", user.getRole());

        return ResponseEntity.ok(response);
    }

    /**
     * Login request DTO
     */
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}