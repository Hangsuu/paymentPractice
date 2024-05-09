package com.paymentPractice.payment.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CardInformationVO {
    private String cardNumber;
    private String expirationPeriod;
    private String cvc;
}
