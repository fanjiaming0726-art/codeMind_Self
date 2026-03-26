package com.example.codemind_self.common.result;


import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(200,"操作成功"),
    FAILED(500,"服务器内部错误"),
    VALIDATE_FAILED(400,"参数校验失败"),
    UNAUTHORIZED(401,"未登录或Token已过期"),
    FORBIDDEN(403,"无权限访问"),
    NOT_FOUND(404,"资源不存在");



    private final int code;
    private final String message;

    ResultCode(int code,String message){
        this.code = code;
        this.message = message;
    }

}
