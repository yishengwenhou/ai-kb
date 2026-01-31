package com.itpan.backend.model.dto.user;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String keyword;

    private Long deptId;

    private Integer status;

    private Integer role;

    private Integer pageNum = 1;

    private Integer pageSize = 10;

}
