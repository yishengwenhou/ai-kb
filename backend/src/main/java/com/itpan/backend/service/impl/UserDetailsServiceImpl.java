package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.security.CustomUserDetails;
import com.itpan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 查数据库
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 2. 获取权限 (保持你原有的逻辑)
        List<GrantedAuthority> authorities = getUserAuthority(user.getId());

        // 3. 【关键修改】返回自定义的 CustomUserDetails
        return new CustomUserDetails(user, authorities);
    }
    
    private List<GrantedAuthority> getUserAuthority(Long userId) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // 这里应该根据用户ID查询用户的角色和权限
        // 为了简化，暂时返回空列表，实际应用中需要查询数据库
        // 查询用户角色
        // QueryWrapper<SysUserRole> userRoleWrapper = new QueryWrapper<>();
        // userRoleWrapper.eq("user_id", userId);
        // List<SysUserRole> userRoles = sysUserRoleService.list(userRoleWrapper);
        
        // 根据角色查询权限
        // for (SysUserRole userRole : userRoles) {
        //     // 查询角色对应的权限
        //     QueryWrapper<SysRolePerm> rolePermWrapper = new QueryWrapper<>();
        //     rolePermWrapper.eq("role_id", userRole.getRoleId());
        //     List<SysRolePerm> rolePerms = sysRolePermService.list(rolePermWrapper);
        //     
        //     for (SysRolePerm rolePerm : rolePerms) {
        //         // 根据permId查询权限信息
        //         SysPermission permission = sysPermissionService.getById(rolePerm.getPermId());
        //         if (permission != null) {
        //             authorities.add(new SimpleGrantedAuthority(permission.getPermKey()));
        //         }
        //     }
        // }
        
        return authorities;
    }
}