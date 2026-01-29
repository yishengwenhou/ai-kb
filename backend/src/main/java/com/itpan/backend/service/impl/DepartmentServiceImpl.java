package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.mapper.DepartmentMapper;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.vo.DepartmentVo;
import com.itpan.backend.service.DepartmentService;
import com.itpan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final UserService userService;

    @Override
    public List<DepartmentVo> getDepartmentTree(Long parentId) {
        // 查询指定父部门下的所有子部门
        List<Department> departments = this.list(new LambdaQueryWrapper<Department>()
                .eq(parentId != null, Department::getParentId, parentId)
                .isNull(parentId == null, Department::getParentId)  // 如果parentId为null，则查询根部门
                .orderByAsc(Department::getSort));  // 按排序字段升序排列

        // 将部门实体转换为VO并构建树形结构
        return departments.stream().map(dept -> {
            DepartmentVo vo = DepartmentVo.builder()
                    .id(dept.getId())
                    .parentId(dept.getParentId())
                    .deptName(dept.getDeptName())
                    .sort(dept.getSort())
                    .build();

            // 递归获取子部门
            List<Department> children = getChildrenDepartments(dept.getId());
            if (!children.isEmpty()) {
                vo.setChildren(children);
            }

            return vo;
        }).collect(Collectors.toList());
    }

    private List<Department> getChildrenDepartments(Long parentId) {
        return this.list(new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, parentId)
                .orderByAsc(Department::getSort));
    }

    @Override
    public Department getDepartmentById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean createDepartment(Department department) {
        // 设置默认值
        if (department.getStatus() == null) {
            department.setStatus(0); // 默认正常状态
        }
        return baseMapper.insert(department) > 0;
    }

    @Override
    public Long checkChildrenExist(Long id) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<Department>().eq(Department::getParentId, id)
        );
    }

    @Override
    public IPage<DepartmentVo> getPageList(String keyword, Long pageNum, Long pageSize) {

        Page<Department> page = new Page<>(pageNum, pageSize);
        IPage<Department> departmentPage = baseMapper.selectPage(page, new LambdaQueryWrapper<Department>()
                .like(StringUtils.hasText(keyword), Department::getDeptName, keyword));
        
        // 收集所有 leader IDs 一次性查询
        Set<Long> leaderIds = departmentPage.getRecords().stream()
                .map(Department::getLeader)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Long, User> userMap;
        if (!leaderIds.isEmpty()) {
            List<User> users = userService.listByIds(new ArrayList<>(leaderIds));
            userMap = users.stream().collect(Collectors.toMap(User::getId, user -> user));
        } else {
            userMap = new HashMap<>();
        }

        IPage<DepartmentVo> departmentVoPage = departmentPage.convert(dept -> {
            User leader = null;
            if (dept.getLeader() != null && userMap.containsKey(dept.getLeader())) {
                leader = userMap.get(dept.getLeader());
            }
            return DepartmentVo.builder()
                    .id(dept.getId())
                    .deptName(dept.getDeptName())
                    .parentId(dept.getParentId())
                    .sort(dept.getSort())
                    .status(dept.getStatus())
                    .leader(leader)
                    .build();
        });
        
        return departmentVoPage;
    }

    @Override
    public Department updateDept(Department department) {
        return baseMapper.updateById(department) > 0 ? department : null;
    }

    @Override
    public boolean deleteDept(Long id) {
        return false;
    }
}