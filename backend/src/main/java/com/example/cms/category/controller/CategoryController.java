package com.example.cms.category.controller;

import com.example.cms.category.dto.CategoryCreateDTO;
import com.example.cms.category.dto.CategoryUpdateDTO;
import com.example.cms.category.service.CategoryService;
import com.example.cms.category.vo.CategoryVO;
import com.example.cms.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "分类管理", description = "分类树、CRUD")
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类树")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('category:list')")
    public ApiResponse<List<CategoryVO>> tree() {
        return ApiResponse.success(categoryService.getTree());
    }

    @Operation(summary = "全部分类(扁平)")
    @GetMapping
    @PreAuthorize("hasAuthority('category:list')")
    public ApiResponse<List<CategoryVO>> list() {
        return ApiResponse.success(categoryService.getAll());
    }

    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('category:list')")
    public ApiResponse<CategoryVO> detail(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getById(id));
    }

    @Operation(summary = "新增分类")
    @PostMapping
    @PreAuthorize("hasAuthority('category:create')")
    public ApiResponse<Long> create(@Valid @RequestBody CategoryCreateDTO dto) {
        return ApiResponse.success(categoryService.create(dto));
    }

    @Operation(summary = "编辑分类")
    @PutMapping
    @PreAuthorize("hasAuthority('category:update')")
    public ApiResponse<Void> update(@Valid @RequestBody CategoryUpdateDTO dto) {
        categoryService.update(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用分类")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('category:update')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        categoryService.updateStatus(id, body.get("status"));
        return ApiResponse.success();
    }
}
