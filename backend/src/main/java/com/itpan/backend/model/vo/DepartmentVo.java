package com.itpan.backend.model.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.entity.User;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.List;

@Data
@Builder
public class DepartmentVo {

    private Long id;

    private Long parentId;

    private User leader;

    private String deptName;

    private int status;

    private Integer sort;

    private List<Department> children;
}