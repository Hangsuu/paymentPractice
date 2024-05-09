package com.paymentPractice.payment.model;

import com.paymentPractice.payment.entity.AmountType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentInformationVO {
    // 관리번호
    private String amountId;
    // 카드번호
    private String cardNumber;
    // 유효기간
    private String expirationPeriod;
    // cvc
    private String cvc;
    // 결제/취소 구분
    private AmountType amountType;
    // 결제/취소 금액
    private int amount;
    // 부가가치세
    private int vat;
}
