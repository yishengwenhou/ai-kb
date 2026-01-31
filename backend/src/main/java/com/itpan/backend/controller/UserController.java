package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.common.constants.UserConstant;
import com.itpan.backend.model.dto.PasswordChangeDTO;
import com.itpan.backend.model.dto.user.UserCreateDTO;
import com.itpan.backend.model.dto.user.UserQueryDTO;
import com.itpan.backend.model.dto.user.UserUpdateAdminDTO;
import com.itpan.backend.model.dto.user.UserUpdateDTO;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.vo.UserVO;
import com.itpan.backend.service.UserService;
import com.itpan.backend.util.UserContext;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
;

@RestController
@RequestMapping("/api/user")
@Validated
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
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserUpdateDTO updateDTO) {
        boolean success = userService.updateProfile(updateDTO);
        if (!success) {
            return ResponseEntity.status(400).body("用户信息修改失败");
        }
        return ResponseEntity.ok("用户信息修改成功");
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody PasswordChangeDTO passwordDTO) {
        boolean success = userService.updatePassword(passwordDTO);
        if (!success) {
            return ResponseEntity.status(400).body("密码修改失败");
        }
        return ResponseEntity.ok("密码修改成功");
    }

    @GetMapping("/list")
    public ResponseEntity<IPage<UserVO>> listUser(UserQueryDTO userQueryDTO) {
        IPage<UserVO> pageList = userService.getPageList(userQueryDTO);
        return ResponseEntity.ok(pageList);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        User user = userService.createUser(userCreateDTO);
        return ResponseEntity.ok(user);
    }

   @PostMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateAdminDTO updateDTO) {
       User user = userService.updateUserByAdmin(updateDTO);
       return ResponseEntity.ok(user);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam("username") String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(400).body("用户不存在");
        }
        user.setPassword(passwordEncoder.encode(UserConstant.BASE_PASSWORD));
        userService.updateById(user);
        return ResponseEntity.ok("密码已重置为：123456");
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody Long id) {
        Boolean success = userService.deleteUser(id);
        if (!success){
            return ResponseEntity.status(400).body("删除用户失败");
        }
        return ResponseEntity.ok("删除用户成功");
    }

}