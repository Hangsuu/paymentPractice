package com.paymentPractice.payment.service;

import com.paymentPractice.payment.model.*;

public interface PaymentService {
    /**
     * 결제
     * @param paymentSO
     * @return PaymentVO
     */
    PaymentResultVO payment(PaymentSO paymentSO);

    /**
     * 전체 취소
     * @param paymentCancellationSO
     * @return PaymentCancellationVO
     */
    PaymentResultVO paymentCancellation(PaymentCancellationSO paymentCancellationSO);

    /**
     * 데이터 조회
     * @param paymentInformationSO
     * @return PaymentInformationVO
     */
    PaymentInformationVO paymentInformation(PaymentInformationSO paymentInformationSO);

    /**
     * 부분 취소
     * @param partialCancellationSO
     * @return PartialCancellationVO
     */
    PaymentResultVO partialCancellation(PartialCancellationSO partialCancellationSO);

}
