package com.itpan.backend.service;

import com.itpan.backend.model.dto.LoginRequest;
import com.itpan.backend.model.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> login(LoginRequest loginRequest);
    boolean register(RegisterRequest registerRequest);
    ResponseEntity<?> refreshToken(String refreshToken);
}
