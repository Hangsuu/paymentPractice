package com.paymentPractice.payment.service;

import com.paymentPractice.payment.model.PaymentResultVO;
import com.paymentPractice.payment.model.PaymentSO;

public interface PaymentCardCheckService {
    /**
     * 한가지 카드번호로 동시에 결제가 진행되지 않도록 결제 진행 시 DB 저장 후 삭제 
     * @param paymentSO
     * @return PaymentResultVO
     */
    PaymentResultVO payment(PaymentSO paymentSO);
}
