package com.itpan.backend.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户更新DTO（管理员更新用户信息）
 */
@Data
public class UserUpdateAdminDTO {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String realName;

    private String username;

    private Integer gender;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Integer status;

    private Long deptId;

    private String avatarUrl;

    private String avatarHash;
}
