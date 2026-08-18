package com.example.cms.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称最长64个字符")
    private String name;

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 64, message = "角色编码最长64个字符")
    private String code;

    private String remark;
    private List<Long> menuIds;
}
