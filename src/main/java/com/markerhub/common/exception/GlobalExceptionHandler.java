package com.markerhub.common.exception;

import com.markerhub.common.lang.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice  //表示定义全局控制器异常处理,如此此类就是一个全局异常捕获类
public class GlobalExceptionHandler {

    //运行时异常
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)   // ExceptionHandler 表示针对性异常处理
    public Result handler(RuntimeException e){
        log.error("运行时异常:--------",e);
        return Result.fail(e.getMessage());
    }


    //Shiro验证异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = ShiroException.class)
    public Result handler(ShiroException e){
        log.error("运行时异常:--------",e);
        return Result.fail(401,e.getMessage(),null);
    }


    //实体校验异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Result handler(MethodArgumentNotValidException e){
        BindingResult bindingResult = e.getBindingResult();
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();
        log.error("实体校验异常:--------",e);

        return Result.fail(objectError.getDefaultMessage());
    }


    // Assert异常
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result handler(IllegalArgumentException e){
        log.error("Assert异常:--------",e);
        return Result.fail(e.getMessage());
    }

}
