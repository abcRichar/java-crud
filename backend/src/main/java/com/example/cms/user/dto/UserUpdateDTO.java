package com.example.cms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @Size(max = 64, message = "昵称最长64个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private String avatar;

    @Min(value = 0, message = "状态只能是0或1")
    @Max(value = 1, message = "状态只能是0或1")
    private Integer status;
    private List<Long> roleIds;
}
