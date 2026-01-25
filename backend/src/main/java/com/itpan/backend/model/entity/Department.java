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
    private Long leader;

    /** 部门状态 (0-正常, 1-停用) */
    @TableField("status")
    private Integer status;

}