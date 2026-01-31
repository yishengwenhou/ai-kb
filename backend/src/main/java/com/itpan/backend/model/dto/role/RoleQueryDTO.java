package com.itpan.backend.model.dto.role;

import lombok.Data;

@Data
public class RoleQueryDTO {
    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String keyword;

    private Integer status;
}