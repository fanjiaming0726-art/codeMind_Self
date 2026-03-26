package com.example.codemind_self.common.exception;


import com.example.codemind_self.common.result.Result;
import com.example.codemind_self.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
// 是spring boot web需要的依赖
@RestControllerAdvice
public class GlobalExceptionHandler {

    /***
     * 之所以设置Result<>内部为问号，主要是声明这个异常捕获方法不在乎data是什么类型的，因为这个异常捕获器捕获的是所有接口的异常错误
     * @param e
     * @return
     */

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e){
        log.warn("业务异常：{}",e.getMessage());
        return Result.fail(e.getMessage());
    }


    // 这是方法参数的校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e){

        // 返回结果列表
        String message = e.getBindingResult()

                // 返回校验失败的参数列表
                        .getFieldErrors()
                        .stream().map(error -> error.getField() + ":" + error.getDefaultMessage())

                // 返回第一个错误，防止返回一大堆错误迷惑用户
                        .findFirst()
                        .orElse("参数校验失败");
        log.warn("参数校验失败：{}",message);
        return Result.fail(ResultCode.VALIDATE_FAILED.getCode(),message);
    }

    @ExceptionHandler(Exception.class)
    public Result<?> ExceptionHandler(Exception e){
        log.warn("业务异常：",e);
        return Result.fail(ResultCode.FAILED);
    }


}
