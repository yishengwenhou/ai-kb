package com.itpan.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.model.dto.department.DeptQueryDTO;
import com.itpan.backend.model.dto.user.UserQueryDTO;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.vo.DepartmentVO;
import com.itpan.backend.model.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    IPage<DepartmentVO> selectDeptVoPage(IPage<DepartmentVO> page, @Param("query") DeptQueryDTO query);
}