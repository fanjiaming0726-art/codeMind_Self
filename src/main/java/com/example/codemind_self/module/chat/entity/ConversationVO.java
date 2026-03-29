package com.example.codemind_self.module.chat.entity;

import lombok.Data;

@Data
public class ConversationVO {
    private Long id;
    private Long kbId;
    private String title;
    private String createTime;
}
