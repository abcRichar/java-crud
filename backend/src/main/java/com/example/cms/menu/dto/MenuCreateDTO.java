package com.example.cms.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuCreateDTO {

    private Long parentId = 0L;

    @NotBlank(message = "路由名称不能为空")
    private String name;

    @NotBlank(message = "显示标题不能为空")
    private String title;

    private String path;
    private String component;
    private String icon;

    @NotBlank(message = "菜单类型不能为空")
    private String type;  // DIRECTORY / MENU / BUTTON

    private String permission;
    private Integer sort = 0;
    private Integer visible = 1;
    private Integer status = 1;
}
