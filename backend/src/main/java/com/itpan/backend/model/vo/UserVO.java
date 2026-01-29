package com.itpan.backend.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    private Long id;

    private String username;

    private String phone;

    private String email;

    private Integer gender;

    private String realName;

    private String deptName;

    private Integer status;

    private String avatarUrl;
}
