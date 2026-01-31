package com.itpan.backend.model.dto.department;

import lombok.Data;

@Data
public class DeptQueryDTO {
    private String keyword;
    private Long pageNum;
    private Long pageSize;
}
