package com.itpan.backend.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门实体类
 */
@Data
@EqualsAndHashCode(callSuper = true) // 包含父类字段的 equals/hashCode
@TableName("department")
public class Department extends BaseEntity {

    /** 父部门ID */
    @TableField("parent_id")
    private Long parentId;

    /** 部门名称 */
    @TableField("dept_name")
    private String deptName;

    /** 显示顺序 */
    @TableField("sort")
    private Integer sort;

    /** 负责人 */
    @TableField("leader")
    private String leader;

    /** 联系电话 */
    @TableField("phone")
    private String phone;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 部门状态 (0-正常, 1-停用) */
    @TableField("status")
    private Integer status;

    // 下面这两个字段数据库里没有，但前端做树形展示时非常有用
    // 使用 @TableField(exist = false) 告诉 MyBatis-Plus 忽略它们
    // @TableField(exist = false)
    // private List<Department> children;
}