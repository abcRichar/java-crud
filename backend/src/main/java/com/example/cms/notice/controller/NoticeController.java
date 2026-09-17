package com.example.cms.notice.controller;

import com.example.cms.common.response.ApiResponse;
import com.example.cms.common.response.PageResult;
import com.example.cms.notice.dto.NoticeCreateDTO;
import com.example.cms.notice.dto.NoticeUpdateDTO;
import com.example.cms.notice.service.NoticeService;
import com.example.cms.notice.vo.NoticeVO;
import com.example.cms.notice.vo.NoticeUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "通知管理", description = "通知CRUD、发布、撤回、已读")
@RestController
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "我的通知(分页)")
    @GetMapping("/my")
    public ApiResponse<PageResult<NoticeUserVO>> myNotices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(noticeService.getMyNotices(page, pageSize));
    }

    @Operation(summary = "通知列表(分页)")
    @GetMapping
    @PreAuthorize("hasAuthority('notice:list')")
    public ApiResponse<PageResult<NoticeVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(noticeService.getPage(keyword, type, status, page, pageSize));
    }

    @Operation(summary = "通知详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('notice:list')")
    public ApiResponse<NoticeVO> detail(@PathVariable Long id) {
        return ApiResponse.success(noticeService.getById(id));
    }

    @Operation(summary = "新增通知")
    @PostMapping
    @PreAuthorize("hasAuthority('notice:create')")
    public ApiResponse<Long> create(@Valid @RequestBody NoticeCreateDTO dto) {
        return ApiResponse.success(noticeService.create(dto));
    }

    @Operation(summary = "编辑通知")
    @PutMapping
    @PreAuthorize("hasAuthority('notice:update')")
    public ApiResponse<Void> update(@Valid @RequestBody NoticeUpdateDTO dto) {
        noticeService.update(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('notice:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "发布通知")
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('notice:publish')")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        noticeService.publish(id);
        return ApiResponse.success();
    }

    @Operation(summary = "撤回通知")
    @PatchMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('notice:withdraw')")
    public ApiResponse<Void> withdraw(@PathVariable Long id) {
        noticeService.withdraw(id);
        return ApiResponse.success();
    }

    @Operation(summary = "标记已读")
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        noticeService.markAsRead(id);
        return ApiResponse.success();
    }

    @Operation(summary = "未读通知数量")
    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount() {
        return ApiResponse.success(noticeService.countUnread());
    }
}
