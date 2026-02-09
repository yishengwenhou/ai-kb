package com.itpan.backend.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户信息更新DTO（用户自己修改个人资料）
 */
@Data
public class UserUpdateDTO {

    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String avatarUrl;

    private String avatarHash;
}
