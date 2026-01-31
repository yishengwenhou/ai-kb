package com.itpan.backend.model.dto.user;


import lombok.Data;

/**
 * 用户创建数据传输对象
 */
@Data
public class UserCreateDTO{
    private String username;

    private String phone;

    private String email;

    private Integer gender;

    private String realName;

    private Long deptId;

    private Integer status;

    private String avatarUrl;

    private String avatarHash;
}
