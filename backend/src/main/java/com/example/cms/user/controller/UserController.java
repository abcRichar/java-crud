package com.example.cms.user.controller;

import com.example.cms.common.dto.PasswordResetDTO;
import com.example.cms.common.dto.StatusUpdateDTO;
import com.example.cms.common.response.ApiResponse;
import com.example.cms.common.response.PageResult;
import com.example.cms.log.annotation.OperationLog;
import com.example.cms.user.dto.UserCreateDTO;
import com.example.cms.user.dto.UserQueryDTO;
import com.example.cms.user.dto.UserUpdateDTO;
import com.example.cms.user.service.UserService;
import com.example.cms.user.vo.UserOptionVO;
import com.example.cms.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户管理", description = "用户CRUD、状态管理、密码重置")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户列表(分页)")
    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public ApiResponse<PageResult<UserVO>> list(UserQueryDTO query) {
        return ApiResponse.success(userService.getPage(query));
    }

    @Operation(summary = "启用用户选项")
    @GetMapping("/options")
    @PreAuthorize("hasAnyAuthority('notice:create','notice:update')")
    public ApiResponse<List<UserOptionVO>> options() {
        return ApiResponse.success(userService.getOptions());
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:list')")
    public ApiResponse<UserVO> detail(@PathVariable Long id) {
        return ApiResponse.success(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    @OperationLog("新增用户")
    public ApiResponse<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        return ApiResponse.success(userService.create(dto));
    }

    @Operation(summary = "编辑用户")
    @PutMapping
    @PreAuthorize("hasAuthority('user:update')")
    @OperationLog("编辑用户")
    public ApiResponse<Void> update(@Valid @RequestBody UserUpdateDTO dto) {
        userService.update(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    @OperationLog("删除用户")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "启用/禁用用户")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateDTO dto) {
        userService.updateStatus(id, dto.getStatus());
        return ApiResponse.success();
    }

    @Operation(summary = "重置密码")
    @PatchMapping("/{id}/password")
    @PreAuthorize("hasAuthority('user:reset')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody PasswordResetDTO dto) {
        userService.resetPassword(id, dto.getPassword());
        return ApiResponse.success();
    }
}
