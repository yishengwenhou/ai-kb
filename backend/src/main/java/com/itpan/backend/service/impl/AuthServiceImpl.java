package com.itpan.backend.service.impl;

import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.dto.LoginRequest;
import com.itpan.backend.model.dto.RegisterRequest;
import com.itpan.backend.model.vo.TokenVo;
import com.itpan.backend.service.AuthService;
import com.itpan.backend.service.UserService;
import com.itpan.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
;
    @Override
    public ResponseEntity<?> login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.username(),
                            loginRequest.password()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户详细信息以生成令牌
            org.springframework.security.core.userdetails.UserDetails userDetails =
                    userDetailsService.loadUserByUsername(authentication.getName());
            String accessToken = jwtUtil.generateAccessToken(userDetails);

            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", 3600); // 访问令牌过期时间
            response.put("username", authentication.getName());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "用户名或密码错误");
            return ResponseEntity.status(401).body(response);
        }
    }

    @Override
    public boolean register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        User existingUser = userService.getUserByUsername(registerRequest.userName());

        if (existingUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 创建新用户
        User user = User.builder()
                .username(registerRequest.userName())
                .password(passwordEncoder.encode(registerRequest.password()))
                .realName(registerRequest.realName())
                .status(0)
                .build();

        return userService.save(user);
    }


    @Override
    public ResponseEntity<?> refreshToken(String refreshToken) {
        String type = jwtUtil.getClaimFromToken(refreshToken, claims -> claims.get("type", String.class));
        if (type == null || !type.equals("refresh")) {
            return ResponseEntity.status(401).body("无效的刷新令牌");
        }
        User user = userService.getUserByUsername(jwtUtil.getUsernameFromToken(refreshToken));
        if(user==null||user.getStatus()!=0){
            return ResponseEntity.status(401).body("用户不存在或已禁用");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        String newRefreshToken = jwtUtil.generateRefreshToken(userDetails);

        return ResponseEntity.ok(new TokenVo(newAccessToken, newRefreshToken));
    }

}
