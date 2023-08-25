package com.hzhg.plm.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    final private int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(BusinessExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.code = exceptionEnum.getCode();
    }
}