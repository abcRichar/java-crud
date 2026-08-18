package com.example.cms.auth.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private List<String> roles;
    private List<String> permissions;
}
