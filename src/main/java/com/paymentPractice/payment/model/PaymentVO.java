package com.paymentPractice.payment.model;

import com.paymentPractice.payment.entity.YesOrNo;
import com.paymentPractice.payment.service.impl.PaymentServiceImpl;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentVO extends CalculatedVatVO{
    public PaymentVO(PaymentSO so) {
        // 결제 유효성 체크
        so.paymentValidationCheck();

        this.cardNumber = so.getCardNumber();
        this.expirationPeriod = so.getExpirationPeriod();
        this.cvc = so.getCvc();
        this.installmentMonths = so.getInstallmentMonths();
        this.amount = so.getAmount();
        this.vat = so.getVat();
        this.userId = so.getUserId() == null ? "tempUserId" : so.getUserId();
        this.vatCalculate(this.vat, this.amount);
    }

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
