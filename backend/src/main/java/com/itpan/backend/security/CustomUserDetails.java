package com.itpan.backend.security;

import com.itpan.backend.model.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final Long deptId;
    private final String realName;

    // === Spring Security 原生字段 ===
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Collection<? extends GrantedAuthority> authorities;

    // 构造函数：把数据库查出来的 User 实体转成 CustomUserDetails
    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.id = user.getId();
        this.deptId = user.getDeptId();
        this.realName = user.getRealName();
        this.username = user.getUsername();
        this.password = user.getPassword();
        // status=0 表示启用，所以 enabled = (status == 0)
        // 注意：之前我们讨论过，如果你的逻辑是 status!=0 为启用，请按需调整
        // 假设：0=启用, 1=禁用
        this.enabled = (user.getStatus() == 0);
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }
}
