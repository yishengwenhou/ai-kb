package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role")
public class Role extends BaseEntity {
    @TableField(value = "role_name")
    private String roleName; // 角色名称 (如：管理员)
    
    @TableField(value = "role_key")
    private String roleKey;  // 角色权限标识 (如：ROLE_ADMIN)
    
    @TableField(value = "sort")
    private Integer sort;    // 显示顺序
}