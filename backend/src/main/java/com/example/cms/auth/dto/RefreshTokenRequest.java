package com.example.cms.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken 不能为空")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String refreshToken;
}
