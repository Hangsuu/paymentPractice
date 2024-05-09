package com.paymentPractice.common.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponseModel {
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String error;
    private final String message;

    public static ResponseEntity<ErrorResponseModel> toResponseEntity(HttpStatus httpStatus, String msg) {
        return ResponseEntity.status(httpStatus).body(
                ErrorResponseModel.builder()
                        .status(httpStatus.value())
                        .error(httpStatus.name())
                        .message(msg)
                        .build()
        );
    }
}
