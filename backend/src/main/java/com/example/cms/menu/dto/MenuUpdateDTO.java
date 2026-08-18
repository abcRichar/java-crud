package com.example.cms.menu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuUpdateDTO {

    @NotNull(message = "ID不能为空")
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
}
