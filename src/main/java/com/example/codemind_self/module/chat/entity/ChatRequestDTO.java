package com.example.codemind_self.module.chat.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequestDTO {

    @NotNull(message = "知识库ID不能为空")
    private Long kbId;

    private Long convId;

    @NotBlank(message = "问题内容不能为空") // 排除纯空格，纯双引号情况
    private String question;

}
