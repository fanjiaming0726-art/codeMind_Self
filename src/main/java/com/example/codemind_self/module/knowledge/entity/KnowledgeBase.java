package com.example.codemind_self.module.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_base")
public class KnowledgeBase {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String description;

    private Integer docCount;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
