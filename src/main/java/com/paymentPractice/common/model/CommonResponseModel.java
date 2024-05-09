package com.paymentPractice.common.model;

import lombok.Data;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class CommonResponseModel<T> {
    private boolean isSuccess;
    private String message;
    private T data;

    private Map<String, Object> extraData;

    public CommonResponseModel(T data) {
        this.data = data;
        this.isSuccess = true;
        this.message = null;
        this.extraData = new HashMap<>();
    }
}
