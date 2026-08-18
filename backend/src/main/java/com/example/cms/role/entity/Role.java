package com.example.cms.role.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Role {

    private Long id;
    private String name;
    private String code;
    private String remark;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
