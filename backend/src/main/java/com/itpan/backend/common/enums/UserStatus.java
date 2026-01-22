package com.itpan.backend.common.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ENABLED(0,"启用"),
    DISABLED(1,"禁用");

    private Integer code;
    private String desc;

    UserStatus(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static UserStatus fromCode(Integer code) {
        for (UserStatus status : UserStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的用户状态码: " + code);
    }

}
