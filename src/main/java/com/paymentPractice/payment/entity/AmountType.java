package com.paymentPractice.payment.entity;

public enum AmountType {
    PAYMENT("결제"),
    CANCEL("결제취소");

    private final String type;

    AmountType(String type) {
        this.type = type;
    }
}
