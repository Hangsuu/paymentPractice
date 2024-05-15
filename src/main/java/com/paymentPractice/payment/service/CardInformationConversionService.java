package com.paymentPractice.payment.service;

import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.model.CardInformationVO;
import com.paymentPractice.payment.model.PaymentSO;
import com.paymentPractice.payment.model.PaymentVO;

public interface CardInformationConversionService {
    // 카드정보 암호화
    void getEncryptedCardInformation(PaymentVO paymentVO);
    // 카드정보 복호화
    CardInformationVO getCardInformation(PaymentEntity paymentEntity);
}
