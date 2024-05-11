package com.paymentPractice.payment.model;

import com.paymentPractice.payment.entity.YesOrNo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentSO {
    // 카드 번호
    private String cardNumber;
    // 유효기간
    private String expirationPeriod;
    // cvc
    private String cvc;
    // 할부개월수
    private int installmentMonths;
    // 결제금액
    private int amount;
    // 부가가치세
    private Integer vat;
    // 유저이름
    private String userId;
}
