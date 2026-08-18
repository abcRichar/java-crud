package com.example.cms.role.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;
    private String name;
    private String code;
    private String remark;
    private Integer status;
    private List<Long> menuIds;
}
