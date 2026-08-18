package com.example.cms.menu.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Menu {

    private Long id;
    private Long parentId;
    private String name;
    private String title;
    private String path;
    private String component;
    private String icon;
    private String type;
    private String permission;
    private Integer sort;
    private Integer visible;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
