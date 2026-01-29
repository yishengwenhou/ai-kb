package com.itpan.backend.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentDTO {

    private Long id; // 更新时必填

    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    /** * 接收前端传来的 ID，而不是整个 User 对象
     * 建议直接叫 leaderId，避免歧义
     */
    private Long leaderId;

    private Integer status;
}
