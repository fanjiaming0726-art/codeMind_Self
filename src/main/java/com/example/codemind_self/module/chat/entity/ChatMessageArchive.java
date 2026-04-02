package com.example.codemind_self.module.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message_archive")
public class ChatMessageArchive {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long convId;

    private String role;

    private String content;

    private Integer tokens;

    private LocalDateTime createTime;
}
