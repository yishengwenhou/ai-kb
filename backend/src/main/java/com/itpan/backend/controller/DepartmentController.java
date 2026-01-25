package com.itpan.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.itpan.backend.model.entity.Department;
import com.itpan.backend.model.vo.DepartmentVo;
import com.itpan.backend.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<DepartmentVo>> listDepartments(@RequestParam(required = false) Long parentId) {
        List<DepartmentVo> departments = departmentService.getDepartmentTree(parentId);
        return ResponseEntity.ok(departments);
    }

    @GetMapping("/list")
    public ResponseEntity<IPage<DepartmentVo>> list(@RequestParam(required = false) String  keyword,
                                                  @RequestParam Long pageNum,
                                                  @RequestParam Long pageSize) {
        IPage<DepartmentVo> departments = departmentService.getPageList(keyword, pageNum, pageSize);
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
    public ResponseEntity<?> createDepartment(@Valid @RequestBody Department department) {
        boolean result = departmentService.createDepartment(department);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @Valid @RequestBody Department department) {
        // 确保ID匹配
        department.setId(id);

        boolean success = departmentService.updateDepartment(department);

        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", department);
            response.put("message", "更新成功");
            response.put("success", true);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "更新失败", "success", false));
        }
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id) {
        boolean success = departmentService.deleteDepartment(id);

        if (success) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "删除成功");
            response.put("success", true);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "删除失败", "success", false));
        }
    }
}