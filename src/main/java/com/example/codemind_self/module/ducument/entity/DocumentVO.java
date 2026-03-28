package com.example.codemind_self.module.ducument.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DocumentVO {

    private Long id;
    private String kbId;
    private String fileName;
    private String fileUrl;
    private Long fileSize; // multipartFile.getSize()的返回对象就是Long类型的
    private Integer status;
    private String statusDesc;
    private Integer chunkCount;
    private LocalDateTime createTime;

}
