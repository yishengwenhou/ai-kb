package com.itpan.backend.model.dto.department;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeptCreateDTO {

    private Long parentId;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private Long leader;

    private Integer status;
}
