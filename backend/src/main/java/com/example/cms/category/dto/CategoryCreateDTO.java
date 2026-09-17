package com.example.cms.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CategoryCreateDTO {

    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String slug;
    private Integer sort = 0;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status = 1;
}
