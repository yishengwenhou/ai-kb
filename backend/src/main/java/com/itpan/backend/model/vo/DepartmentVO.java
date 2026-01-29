package com.itpan.backend.model.vo;


import com.itpan.backend.model.entity.User;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentVO {

    private Long id;

    private Long parentId;

    private User leader;

    private String deptName;

    private int status;

    private Integer sort;

    private List<DepartmentVO> children;
}