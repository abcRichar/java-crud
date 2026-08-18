package com.example.cms.category.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {

    private Long id;
    private Long parentId;
    private String name;
    private String slug;
    private Integer sort;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
