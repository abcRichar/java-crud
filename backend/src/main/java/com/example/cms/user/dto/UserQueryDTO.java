package com.example.cms.user.dto;

import lombok.Data;

@Data
public class UserQueryDTO {

    private String keyword;
    private Integer status;
    private Integer page = 1;
    private Integer pageSize = 10;
}
