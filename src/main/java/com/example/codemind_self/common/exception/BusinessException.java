package com.example.codemind_self.common.exception;

import com.example.codemind_self.common.result.ResultCode;
import lombok.Getter;


@Getter
public class BusinessException extends RuntimeException{

    private final int code;

    public BusinessException(String message){
        super(message);
        this.code = ResultCode.FAILED.getCode();;
    }

    public BusinessException(ResultCode resultCode){
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code,String message){
        super(message);
        this.code = code;
    }


}
