package com.paymentPractice.payment.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {
    SUCCESS("승인"),
    PARTIAL_CANCELLATION("부분취소"),
    CANCELLATION("취소");

    private final String status;

    PaymentStatus(String status) {
        this.status = status;
    }
}
