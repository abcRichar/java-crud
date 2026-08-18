package com.example.cms.user.dto;

import jakarta.validation.constraints.Email;
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
    private Integer status;
    private List<Long> roleIds;
}
