package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.mapper.DepartmentMapper;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.vo.DepartmentVo;
import com.itpan.backend.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

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
    public boolean updateDepartment(Department department) {
        return baseMapper.updateById(department) > 0;
    }

    @Override
    public boolean deleteDepartment(Long id) {
        // 检查是否存在子部门
        Long childrenCount = checkChildrenExist(id);
        if (childrenCount > 0) {
            throw new RuntimeException("该部门下仍有 " + childrenCount + " 个子部门，请先清空子部门后再删除部门！");
        }

        // 执行删除
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public Long checkChildrenExist(Long id) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<Department>().eq(Department::getParentId, id)
        );
    }
}