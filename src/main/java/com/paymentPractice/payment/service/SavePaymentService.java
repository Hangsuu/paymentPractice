package com.paymentPractice.payment.service;

import com.paymentPractice.payment.entity.AmountEntity;
import com.paymentPractice.payment.model.PaymentVO;

public interface SavePaymentService {
    String savePaymentResult(PaymentVO paymentVO);
}
