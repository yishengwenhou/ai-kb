package com.itpan.backend.controller;


import com.itpan.backend.model.dto.auth.RefreshTokenRequest;
import com.itpan.backend.model.dto.auth.LoginRequest;
import com.itpan.backend.model.dto.auth.RegisterRequest;
import com.itpan.backend.model.vo.TokenVO;
import com.itpan.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {

        try {
            boolean saved = authService.register(registerRequest);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "注册成功");
        return ResponseEntity.ok(response);

    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        TokenVO tokenVo = request.getTokenVo();
        return authService.refreshToken(tokenVo.getRefreshToken());
    }
}
