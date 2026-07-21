package com.example.api_gateway.controller;

import com.example.api_gateway.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "guest");
        String role = body.getOrDefault("role", "USER");

        String token = jwtUtil.generateToken(username, role);

        return ResponseEntity.ok(Map.of(
                "username", username,
                "role", role,
                "token", token
        ));
    }
}
