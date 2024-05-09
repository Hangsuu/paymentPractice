package com.paymentPractice.payment.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResultVO {
    // unique id
    private String amountId;
    // 전송 데이터
    private String stringData;
}
