package com.paymentPractice.payment.service;

import com.paymentPractice.payment.model.PartialCancellationSO;
import com.paymentPractice.payment.model.PaymentSO;

public interface ValidationCheckService {
    // 결제 유효성 체크
    void paymentValidationCheck(PaymentSO paymentSO);
    // 부분취소  유효성 체크
    void partialCancellationValidationCheck(PartialCancellationSO partialCancellationSO);
}
