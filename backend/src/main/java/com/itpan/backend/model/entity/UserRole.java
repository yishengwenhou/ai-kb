package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_role")
public class UserRole {
    @TableField(value = "user_id")
    private Long userId;
    
    @TableField(value = "role_id")
    private Long roleId;
}