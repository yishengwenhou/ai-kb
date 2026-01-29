package com.itpan.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.entity.Document;
import com.itpan.backend.model.vo.DepartmentVo;

import java.util.List;

public interface DepartmentService extends IService<Department> {
    /**
     * 获取部门列表（支持树形结构）
     * @param parentId 父部门ID，为null时获取根部门
     * @return 部门列表
     */
    List<DepartmentVo> getDepartmentTree(Long parentId);

    /**
     * 根据ID获取部门详情
     * @param id 部门ID
     * @return 部门实体
     */
    Department getDepartmentById(Long id);

    /**
     * 创建部门
     * @param department 部门实体
     * @return 是否创建成功
     */
    boolean createDepartment(Department department);

    /**
     * 检查部门是否存在子部门
     * @param id 部门ID
     * @return 子部门数量
     */
    Long checkChildrenExist(Long id);

    IPage<DepartmentVo> getPageList(String keyword, Long pageNum, Long pageSize);

    boolean deleteDept(Long id);

    Department updateDept(Department department);
}