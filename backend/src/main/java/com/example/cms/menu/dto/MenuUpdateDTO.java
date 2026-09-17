package com.example.cms.menu.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

    @Pattern(regexp = "DIRECTORY|MENU|BUTTON", message = "菜单类型不正确")
    private String type;
    private String permission;
    private Integer sort;

    @Min(value = 0, message = "是否可见只能是0或1")
    @Max(value = 1, message = "是否可见只能是0或1")
    private Integer visible;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status;
}
