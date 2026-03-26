package com.example.codemind_self.common.result;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result(int code,String message,T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(){
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(),null);
    }

    public static <T> Result<T> success(T data){
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> fail(String message){
        return new Result<>(ResultCode.FAILED.getCode(), message, null);
    }

    public static <T> Result<T> fail(ResultCode resultCode){
        return new Result<>(resultCode.getCode(),resultCode.getMessage(),null);
    }

    public static <T> Result<T> fail(int code,String message){
        return new Result<>(code,message,null);
    }

}
