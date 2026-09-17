package com.example.cms.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class NoticeUpdateDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotBlank(message = "通知标题不能为空")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    private String content;

    @Pattern(regexp = "SYSTEM|NOTICE|ACTIVITY|MESSAGE", message = "通知类型不正确")
    private String type;
    private List<Long> userIds;
}
