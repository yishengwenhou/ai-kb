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
    private String roleName;
    
    @TableField(value = "role_key")
    private String roleKey;
    
    @TableField(value = "sort")
    private Integer sort;

    @TableField(value = "status")
    private Integer status;
}