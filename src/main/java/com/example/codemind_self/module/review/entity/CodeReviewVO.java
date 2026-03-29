package com.example.codemind_self.module.review.entity;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CodeReviewVO {

    private Long id;
    private String fileName;
    private Integer issuesCount;
    private String report;
    private LocalDateTime createTime;

}
