package com.example.codemind_self.module.chat.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long convId;

    private String role; // user/consistent

    private String content;

    private Integer tokens; // 统计消息的tokens额度

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
