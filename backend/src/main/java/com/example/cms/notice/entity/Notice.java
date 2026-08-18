package com.example.cms.notice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notice {

    private Long id;
    private String title;
    private String content;
    private String type;  // SYSTEM / NOTICE / ACTIVITY / MESSAGE
    private String status;  // DRAFT / PUBLISHED / WITHDRAWN
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
