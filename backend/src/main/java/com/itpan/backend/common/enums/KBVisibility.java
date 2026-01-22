package com.itpan.backend.common.enums;

public enum KBVisibility {
    PUBLIC(0,"私有"),
    PRIVATE(1,"公开"),
    DEPARTMENT(2,"部门内");

    private Integer code;
    private String desc;
    KBVisibility(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public static KBVisibility fromCode(Integer code) {
        for (KBVisibility status : KBVisibility.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("无效的KB可见性码: " + code);
    }
}
