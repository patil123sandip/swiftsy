package com.example.swiftsy_app.service;

import com.example.swiftsy_app.dto.AuthResponse;
import com.example.swiftsy_app.dto.LoginRequest;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final Environment env;
    private final Map<String, String> activeTokens = new ConcurrentHashMap<>();

    public AuthService(Environment env) {
        this.env = env;
    }

    public AuthResponse login(LoginRequest request) {
        String expectedPassword = env.getProperty("app.users." + request.getUsername());
        
        if (expectedPassword != null && expectedPassword.equals(request.getPassword())) {
            String token = UUID.randomUUID().toString();
            activeTokens.put(token, request.getUsername());
            return new AuthResponse(token, request.getUsername());
        }
        throw new RuntimeException("Invalid username or password");
    }

    public boolean validateToken(String token) {
        return activeTokens.containsKey(token);
    }
    
    public void logout(String token) {
        activeTokens.remove(token);
    }
}
