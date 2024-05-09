package com.paymentPractice.common.exception;

import com.paymentPractice.common.model.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {
    private HttpStatus status;
    private String message;
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.message = message;
    }
    public CustomException(ErrorCode errorCode){
        this.status = errorCode.getHttpStatus();
        this.message = errorCode.getDetail();
    }
}
