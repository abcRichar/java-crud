package com.example.cms.article.dto;

import jakarta.validation.constraints.NotBlank;
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
    private String status = "DRAFT";
}
