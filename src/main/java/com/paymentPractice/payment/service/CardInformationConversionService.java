package com.paymentPractice.payment.service;

import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.model.CardInformationVO;
import com.paymentPractice.payment.model.PaymentSO;

public interface CardInformationConversionService {
    // 카드정보 암호화
    String getEncryptedCardInformation(PaymentSO paymentSO);
    // 카드정보 복호화
    CardInformationVO getCardInformation(PaymentEntity paymentEntity);
    // 카드정보 마스킹
    String maskingCardNumber(String cardNumber);
}
