package com.example.cms.role.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class RoleUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;
    private String name;

    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_-]*$", message = "角色编码只能包含字母、数字、下划线和短横线")
    private String code;
    private String remark;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status;
    private List<Long> menuIds;
}
