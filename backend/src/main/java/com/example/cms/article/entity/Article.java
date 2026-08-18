package com.example.cms.article.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Article {

    private Long id;
    private String title;
    private String summary;
    private String content;
    private String cover;
    private Long categoryId;
    private Long authorId;
    private String authorName;
    private String status;  // DRAFT / PUBLISHED / OFFLINE
    private Integer viewCount;
    private Integer likeCount;
    private String seoTitle;
    private String seoDescription;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
