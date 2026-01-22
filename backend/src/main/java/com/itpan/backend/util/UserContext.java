package com.itpan.backend.util;

import com.itpan.backend.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class UserContext {

    /**
     * 获取当前登录用户 (从内存中直接拿，不查库)
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        throw new RuntimeException("当前用户未登录或登录信息无效");
    }

    /**
     * 快捷获取用户ID
     */
    public static Long getUserId() {
        return getCurrentUser().getId();
    }

    /**
     * 快捷获取部门ID
     */
    public static Long getDeptId() {
        return getCurrentUser().getDeptId();
    }
}
