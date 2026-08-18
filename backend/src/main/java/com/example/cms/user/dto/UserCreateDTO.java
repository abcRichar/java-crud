package com.example.cms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserCreateDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 64, message = "用户名长度3-64个字符")
    private String username;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 64, message = "昵称最长64个字符")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度6-32个字符")
    private String password;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private String avatar;
    private List<Long> roleIds;
}
