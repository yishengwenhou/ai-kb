package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("role_perm")
public class RolePerm {
    @TableField(value = "role_id")
    private Long roleId;
    
    @TableField(value = "perm_id")
    private Long permId;
}