package com.paymentPractice.payment.service;

import com.paymentPractice.payment.entity.AmountEntity;
import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.model.PartialCancellationVO;
import com.paymentPractice.payment.model.PaymentCancellationSO;
import com.paymentPractice.payment.model.PaymentVO;

public interface SavePaymentService {
    String savePaymentResult(PaymentVO paymentVO);

    String savePaymentCancellation(PaymentEntity paymentEntity);

    String savePartialCancellation(PartialCancellationVO partialCancellationVO, PaymentEntity paymentEntity);
}
