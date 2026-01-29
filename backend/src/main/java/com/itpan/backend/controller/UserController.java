package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.service.UserService;
import com.itpan.backend.util.UserContext;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        String username = UserContext.getCurrentUser().getUsername();
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody User updateData) {
        User currentUser = UserContext.getCurrentUser();
        User user = userService.getById(currentUser.getId());
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }

        // 更新可修改的字段
        if (StringUtils.hasText(updateData.getRealName())) {
            user.setRealName(updateData.getRealName());
        }
        if (StringUtils.hasText(updateData.getEmail())) {
            user.setEmail(updateData.getEmail());
        }
        if (StringUtils.hasText(updateData.getPhone())) {
            user.setPhone(updateData.getPhone());
        }

        userService.updateById(user);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");

        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return ResponseEntity.status(400).body("参数不能为空");
        }

        User currentUser = UserContext.getCurrentUser();
        User user = userService.getById(currentUser.getId());
        if (user == null) {
            return ResponseEntity.status(404).body("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(400).body("原密码不正确");
        }

        // 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);

        return ResponseEntity.ok("密码修改成功");
    }

    @GetMapping("/list")
    public ResponseEntity<IPage<User>> listUser(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        IPage<User> pageList = userService.getPageList(keyword, pageNum, pageSize);
        return ResponseEntity.ok(pageList);
    }

   @PostMapping("/update")
    public ResponseEntity<?> updateUser(@RequestParam("user") User user) {
       User user1 = userService.updateUser(user);
       return ResponseEntity.ok(user1);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("username") String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(400).body("用户不存在");
        }
        user.setPassword(passwordEncoder.encode("1234567"));
        userService.updateById(user);
        return ResponseEntity.ok("密码已重置为：123456");
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        return ResponseEntity.status(501).body("暂未开放该接口");
    }

}