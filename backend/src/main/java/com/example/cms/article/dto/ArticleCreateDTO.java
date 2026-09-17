package com.example.cms.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ArticleCreateDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;
    private String content;
    private String cover;
    private Long categoryId;
    private String seoTitle;
    private String seoDescription;

    @Pattern(regexp = "DRAFT|PUBLISHED|OFFLINE", message = "文章状态不正确")
    private String status = "DRAFT";
}
