package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@TableName("User")
public class User extends BaseEntity {
    @TableField(value = "username")
    private String username;  // 用户名
    
    @TableField(value = "password")
    private String password;  // 加密后的密码
    
    @TableField(value = "real_name")
    private String realName;  // 真实姓名
    
    @TableField(value = "dept_id")
    private Long deptId;      // 所属部门ID
    
    @TableField(value = "status")
    private Integer status;   // 状态 (0-启用, 1-禁用)
}