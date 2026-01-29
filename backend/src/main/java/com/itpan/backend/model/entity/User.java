package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("User")
public class User extends BaseEntity {
    @TableField(value = "username")
    private String username;  // 用户名

    @TableField(value = "phone")
    private String phone;     // 手机号

    @TableField(value = "email")
    private String email;

    @TableField(value = "gender")
    private Integer gender;
    
    @TableField(value = "password")
    private String password;
    
    @TableField(value = "real_name")
    private String realName;
    
    @TableField(value = "dept_id")
    private Long deptId;

    // 状态 (0-启用, 1-禁用)
    @TableField(value = "status")
    private Integer status;

    @TableField(value = "avatar_url")
    private String avatarUrl;

    @TableField(value = "avatar_hash")
    private String avatarHash;
}