package com.example.cms.article.controller;

import com.example.cms.article.dto.ArticleCreateDTO;
import com.example.cms.article.dto.ArticleUpdateDTO;
import com.example.cms.article.service.ArticleService;
import com.example.cms.article.vo.ArticleVO;
import com.example.cms.common.response.ApiResponse;
import com.example.cms.common.response.PageResult;
import com.example.cms.log.annotation.OperationLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "文章管理", description = "文章CRUD、发布、下线")
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "文章列表(分页)")
    @GetMapping
    @PreAuthorize("hasAuthority('article:list')")
    public ApiResponse<PageResult<ArticleVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(articleService.getPage(keyword, categoryId, status, page, pageSize));
    }

    @Operation(summary = "文章详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('article:list')")
    public ApiResponse<ArticleVO> detail(@PathVariable Long id) {
        ArticleVO vo = articleService.getById(id);
        articleService.incrementViewCount(id);
        return ApiResponse.success(vo);
    }

    @Operation(summary = "新增文章")
    @PostMapping
    @PreAuthorize("hasAuthority('article:create')")
    @OperationLog("新增文章")
    public ApiResponse<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        return ApiResponse.success(articleService.create(dto));
    }

    @Operation(summary = "编辑文章")
    @PutMapping
    @PreAuthorize("hasAuthority('article:update')")
    @OperationLog("编辑文章")
    public ApiResponse<Void> update(@Valid @RequestBody ArticleUpdateDTO dto) {
        articleService.update(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('article:delete')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ApiResponse.success();
    }

    @Operation(summary = "批量删除文章")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('article:delete')")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        articleService.batchDelete(body.get("ids"));
        return ApiResponse.success();
    }

    @Operation(summary = "发布文章")
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('article:publish')")
    @OperationLog("发布文章")
    public ApiResponse<Void> publish(@PathVariable Long id) {
        articleService.publish(id);
        return ApiResponse.success();
    }

    @Operation(summary = "下线文章")
    @PatchMapping("/{id}/offline")
    @PreAuthorize("hasAuthority('article:offline')")
    public ApiResponse<Void> offline(@PathVariable Long id) {
        articleService.offline(id);
        return ApiResponse.success();
    }
}
