package com.example.cms.role.controller;

import com.example.cms.common.dto.StatusUpdateDTO;
import com.example.cms.common.response.ApiResponse;
import com.example.cms.common.response.PageResult;
import com.example.cms.role.dto.RoleCreateDTO;
import com.example.cms.role.dto.RoleUpdateDTO;
import com.example.cms.role.service.RoleService;
import com.example.cms.role.vo.RoleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "角色管理", description = "角色CRUD、权限分配")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "角色列表(分页)")
    @GetMapping
    @PreAuthorize("hasAuthority('role:list')")
    public ApiResponse<PageResult<RoleVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(roleService.getPage(keyword, status, page, pageSize));
    }

    @Operation(summary = "全部角色(下拉选择用)")
    @GetMapping("/all")
    public ApiResponse<List<RoleVO>> all() {
        return ApiResponse.success(roleService.getAll());
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:list')")
    public ApiResponse<RoleVO> detail(@PathVariable Long id) {
        return ApiResponse.success(roleService.getById(id));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public ApiResponse<Long> create(@Valid @RequestBody RoleCreateDTO dto) {
        return ApiResponse.success(roleService.create(dto));
    }

    @Operation(summary = "编辑角色")
    @PutMapping
    @PreAuthorize("hasAuthority('role:update')")
    public ApiResponse<Void> update(@Valid @RequestBody RoleUpdateDTO dto) {
        roleService.update(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用角色")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('role:update')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        roleService.updateStatus(id, dto.getStatus());
        return ApiResponse.success();
    }
}
