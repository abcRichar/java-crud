package com.example.cms.menu.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuVO {

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private List<MenuVO> children;
}
