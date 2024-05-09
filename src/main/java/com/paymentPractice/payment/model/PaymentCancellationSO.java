package com.paymentPractice.payment.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentCancellationSO {
    // 관리번호
    private String amountId;
    // 취소금액
    private int amount;
}
