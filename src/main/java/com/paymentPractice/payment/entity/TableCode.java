package com.paymentPractice.payment.entity;

public enum TableCode {
    PA("PAYMENT"),
    AM("AMOUNT");

    private final String tableName;

    TableCode(String tableName) {
        this.tableName = tableName;
    }
}
