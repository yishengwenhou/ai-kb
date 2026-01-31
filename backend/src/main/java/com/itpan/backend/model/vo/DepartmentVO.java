package com.itpan.backend.model.vo;


import com.itpan.backend.model.entity.User;
import lombok.*;

import java.time.LocalDateTime;
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

    private LocalDateTime createTime;

    private List<DepartmentVO> children;
}