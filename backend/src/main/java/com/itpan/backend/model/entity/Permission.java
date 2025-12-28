package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permission")
public class Permission extends BaseEntity {
    @TableField(value = "perm_name")
    private String permName; // 权限名称
    
    @TableField(value = "perm_key")
    private String permKey;  // 权限标识 (如：kb:doc:delete)
    
    @TableField(value = "url")
    private String url;      // 对应接口路径
    
    @TableField(value = "type")
    private Integer type;    // 类型 (0-菜单, 1-按钮)
}