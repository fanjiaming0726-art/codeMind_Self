package com.example.codemind_self.module.ducument.entity;


import lombok.Getter;

@Getter
public enum DocumentStatusEnum {

    UPLOADING(0,"上传中"),
    PARSING(1,"解析中"),
    READY(2,"已就绪"),
    FAILED(3,"解析失败");

    private int code;
    private String desc;

    DocumentStatusEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(int code){
        for(DocumentStatusEnum statusEnum : values()){
            if(statusEnum.code == code){
                return statusEnum.desc;
            }
        }
        return "未知状态";
    }
}
