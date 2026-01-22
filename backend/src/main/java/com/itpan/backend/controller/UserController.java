package com.itpan.backend.controller;

import com.itpan.backend.model.entity.User;
import com.itpan.backend.service.UserService;
import com.itpan.backend.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String username = UserContext.getCurrentUser().getUsername();
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

   @PostMapping("/update")
    public ResponseEntity<?> updateUser() {
        return ResponseEntity.status(501).body("暂未开放该接口");
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        return ResponseEntity.status(501).body("暂未开放该接口");
    }

}