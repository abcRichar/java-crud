package com.example.cms.category.dto;

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
    private Integer status;
}
