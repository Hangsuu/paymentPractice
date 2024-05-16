package com.paymentPractice.payment.service;

import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.model.CardInformationVO;
import com.paymentPractice.payment.model.PaymentVO;

public interface TransferAndGetStringDataService {
    String paymentSendingData(String amountId, PaymentVO paymentVO);

    String paymentCancellationSendingData(String amountId, PaymentEntity paymentEntity);
}
