package com.itpan.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itpan.backend.mapper.DepartmentMapper;
import com.itpan.backend.model.dto.DepartmentDTO;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.entity.User;
import com.itpan.backend.model.vo.DepartmentVO;
import com.itpan.backend.service.DepartmentService;
import com.itpan.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final UserService userService;

    @Override
    public List<DepartmentVO> getDepartmentTree() {

        List<Department> departments = baseMapper.selectList(null);

        // 将部门实体转换为VO并构建树形结构
        List<DepartmentVO> vos = departments.stream().map(dept -> {
            DepartmentVO vo = DepartmentVO.builder()
                    .id(dept.getId())
                    .parentId(dept.getParentId())
                    .deptName(dept.getDeptName())
                    .children(new ArrayList<>())
                    .build();
            return vo;
        }).collect(Collectors.toList());

        Map<Long, DepartmentVO> voMap = vos.stream().collect(
                Collectors.toMap(DepartmentVO::getId, vo -> vo)
        );
        DepartmentVO dummy = DepartmentVO.builder()
                .children(new ArrayList<>())
                .build();
        for (DepartmentVO vo : vos){
            Long parentId = vo.getParentId();
            if(parentId==null){
                dummy.getChildren().add(vo);
            }else{
                DepartmentVO parent = voMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    dummy.getChildren().add(vo);
                }
            }
        }

        return dummy.getChildren();
    }

    @Override
    public Department getDepartmentById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public boolean createDepartment(DepartmentDTO departmentDTO) {
        Department department = Department.builder()
                .parentId(departmentDTO.getParentId())
                .deptName(departmentDTO.getDeptName())
                .leader(departmentDTO.getLeaderId())
                .status(departmentDTO.getStatus())
                .build();
        return baseMapper.insert(department) > 0;
    }

    @Override
    public Long checkChildrenExist(Long id) {
        return baseMapper.selectCount(
                new LambdaQueryWrapper<Department>().eq(Department::getParentId, id)
        );
    }

    @Override
    public IPage<DepartmentVO> getPageList(String keyword, Long pageNum, Long pageSize) {

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

        IPage<DepartmentVO> departmentVoPage = departmentPage.convert(dept -> {
            User leader = null;
            if (dept.getLeader() != null && userMap.containsKey(dept.getLeader())) {
                leader = userMap.get(dept.getLeader());
            }
            return DepartmentVO.builder()
                    .id(dept.getId())
                    .deptName(dept.getDeptName())
                    .parentId(dept.getParentId())
                    .status(dept.getStatus())
                    .leader(leader)
                    .build();
        });
        
        return departmentVoPage;
    }

    @Override
    public Department updateDept(DepartmentDTO departmentDTO) {
        Department department = Department.builder()
                .id(departmentDTO.getId())
                .parentId(departmentDTO.getParentId())
                .deptName(departmentDTO.getDeptName())
                .leader(departmentDTO.getLeaderId())
                .status(departmentDTO.getStatus())
                .build();
        return baseMapper.updateById(department) > 0 ? department : null;
    }

    @Override
    public boolean deleteDept(Long id) {
        return baseMapper.deleteById(id)>0;
    }
}