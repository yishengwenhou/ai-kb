package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.model.dto.department.DepartmentDTO;
import com.itpan.backend.model.dto.department.DeptCreateDTO;
import com.itpan.backend.model.dto.department.DeptQueryDTO;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.vo.DepartmentVO;
import com.itpan.backend.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/dept")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门列表（支持树形结构）
     */
    @GetMapping
    public ResponseEntity<List<DepartmentVO>> getDepartmentTree() {
        List<DepartmentVO> departments = departmentService.getDepartmentTree();
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/list")
    public ResponseEntity<IPage<DepartmentVO>> list(DeptQueryDTO deptQueryDTO) {
        IPage<DepartmentVO> departments = departmentService.getPageList(deptQueryDTO);
        return ResponseEntity.ok(departments);
    }

    /**
     * 根据ID获取部门详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDepartment(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }

    /**
     * 创建部门
     */
    @PostMapping
    public ResponseEntity<?> createDepartment(@Valid @RequestBody DeptCreateDTO deptCreateDTO) {
        boolean result = departmentService.createDepartment(deptCreateDTO);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@Valid @RequestBody DepartmentDTO departmentDTO) {
        Department department1 = departmentService.updateDept(departmentDTO);
        return ResponseEntity.ok(department1);
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        boolean success = departmentService.deleteDept(id);
        return ResponseEntity.ok(success);
    }
}