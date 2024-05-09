package com.paymentPractice.common.exception;

import com.paymentPractice.common.model.ErrorResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler({ Exception.class})
    protected ResponseEntity handleException(Exception e) {
        // 발생한 Exception 클래스 추출
        Throwable originalException = e.getCause();
        if(e instanceof CustomException) {
            CustomException customException = (CustomException) e;
            log.error("handle custom exception : ");
            e.printStackTrace();
            return ErrorResponseModel.toResponseEntity(customException.getStatus(),
                    customException.getMessage());
        } else if(originalException != null && originalException instanceof CustomException) {
            CustomException customException = (CustomException) originalException;
            log.error("handle custom forced exception : ");
            e.printStackTrace();
            return ErrorResponseModel.toResponseEntity(customException.getStatus(),
                    customException.getMessage());
        } else {
            log.error("handle RuntimeException");
            e.printStackTrace();
            String errorMessage = (e.getMessage() != null) ? e.toString() : e + ": Unknown cause";
            return ErrorResponseModel.toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
        }
    }
}
