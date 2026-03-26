package com.example.codemind_self.module.knowledge.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class KnowledgeBaseDTO {

    @NotBlank(message = "知识库名字不能为空")
    @Size(max = 100,message = "名称最多100个字符")
    private String name;

    @Size(max = 500,message = "描述最多不超过500字")
    private String description;
}
