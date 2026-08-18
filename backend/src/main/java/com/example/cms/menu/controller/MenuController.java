package com.example.cms.menu.controller;

import com.example.cms.common.response.ApiResponse;
import com.example.cms.menu.dto.MenuCreateDTO;
import com.example.cms.menu.dto.MenuUpdateDTO;
import com.example.cms.menu.service.MenuService;
import com.example.cms.menu.vo.MenuVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理", description = "菜单树、CRUD、用户菜单")
@RestController
@RequestMapping("/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "菜单树(全部)")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('menu:list')")
    public ApiResponse<List<MenuVO>> tree() {
        return ApiResponse.success(menuService.getTree());
    }

    @Operation(summary = "当前用户菜单树(动态路由)")
    @GetMapping("/my-menus")
    public ApiResponse<List<MenuVO>> myMenus() {
        return ApiResponse.success(menuService.getCurrentUserMenuTree());
    }

    @Operation(summary = "菜单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:list')")
    public ApiResponse<MenuVO> detail(@PathVariable Long id) {
        return ApiResponse.success(menuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    @PreAuthorize("hasAuthority('menu:create')")
    public ApiResponse<Long> create(@Valid @RequestBody MenuCreateDTO dto) {
        return ApiResponse.success(menuService.create(dto));
    }

    @Operation(summary = "编辑菜单")
    @PutMapping
    @PreAuthorize("hasAuthority('menu:update')")
    public ApiResponse<Void> update(@Valid @RequestBody MenuUpdateDTO dto) {
        menuService.update(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ApiResponse.success();
    }
}
