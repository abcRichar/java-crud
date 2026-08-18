package com.example.cms.auth.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserInfoVO user;

    public LoginVO(String accessToken, String refreshToken, Long expiresIn, UserInfoVO user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}
