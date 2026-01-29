package com.itpan.backend.common.enums;

import lombok.Getter;

@Getter
public enum KBOwnerType {
    PERSONAL(10, "个人空间"),
    DEPARTMENT(20, "部门空间"),
    ENTERPRISE(30, "公共空间");

    private final Integer code;
    private final String desc;

    KBOwnerType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}