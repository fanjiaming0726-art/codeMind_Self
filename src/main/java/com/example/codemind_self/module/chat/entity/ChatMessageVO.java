package com.example.codemind_self.module.chat.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;
    private String role;
    private String content;
    private LocalDateTime localDateTime;
}
