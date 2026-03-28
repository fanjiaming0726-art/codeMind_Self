package com.example.codemind_self.module.ducument.mq;

import lombok.Data;

@Data
public class DocumentParseMessage {
    private Long documentId;
    private String objectName;
    private Long kbId;
    private Long userId;
}
