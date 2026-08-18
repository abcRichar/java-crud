package com.example.cms.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticleUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;
    private String content;
    private String cover;
    private Long categoryId;
    private String seoTitle;
    private String seoDescription;
}
