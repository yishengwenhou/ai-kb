package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itpan.backend.mapper.RoleMapper;
import com.itpan.backend.model.dto.role.RoleQueryDTO;
import com.itpan.backend.model.entity.Role;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/role")
public class RoleController {

    @Resource
    private RoleMapper roleMapper;
    @GetMapping("/list")
    public ResponseEntity<?> listRole(RoleQueryDTO roleQueryDTO) {
        IPage<Role> rolePage = roleMapper.selectPage(
                new Page<>(roleQueryDTO.getPageNum(), roleQueryDTO.getPageSize()),
                new LambdaQueryWrapper<Role>()
                        .eq(roleQueryDTO.getStatus() != null, Role::getStatus, roleQueryDTO.getStatus())
                        .like(StringUtils.hasText(roleQueryDTO.getKeyword()), Role::getRoleName, roleQueryDTO.getKeyword())
        );
        return ResponseEntity.ok(rolePage);
    }
}
