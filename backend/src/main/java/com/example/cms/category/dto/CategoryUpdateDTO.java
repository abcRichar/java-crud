package com.example.cms.category.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;
    private Long parentId;
    private String name;
    private String slug;
    private Integer sort;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status;
}
