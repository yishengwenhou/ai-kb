package com.itpan.backend.model.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.itpan.backend.model.entity.Department;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Builder
public class DepartmentVo {

    private Long id;

    private Long parentId;


    private String deptName;


    private Integer sort;

    private List<Department> children;
}