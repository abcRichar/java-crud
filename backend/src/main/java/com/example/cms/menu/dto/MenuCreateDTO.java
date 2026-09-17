package com.example.cms.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "DIRECTORY|MENU|BUTTON", message = "菜单类型不正确")
    private String type;  // DIRECTORY / MENU / BUTTON

    private String permission;
    private Integer sort = 0;

    @Min(value = 0, message = "是否可见只能是0或1")
    @Max(value = 1, message = "是否可见只能是0或1")
    private Integer visible = 1;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status = 1;
}
