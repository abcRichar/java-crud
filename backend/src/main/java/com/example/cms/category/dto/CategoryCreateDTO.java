package com.example.cms.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateDTO {

    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String slug;
    private Integer sort = 0;
    private Integer status = 1;
}
