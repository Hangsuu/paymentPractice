package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.payment.model.PaymentResultVO;
import com.paymentPractice.payment.model.PaymentSO;
import com.paymentPractice.payment.service.CardNumberService;
import com.paymentPractice.payment.service.PaymentCardCheckService;
import com.paymentPractice.payment.service.PaymentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentCardCheckServiceImpl implements PaymentCardCheckService {
    private final PaymentService paymentService;
    private final CardNumberService cardNumberService;

    @Override
    // 한가지 카드번호로 동시에 결제가 진행되지 않도록 결제 진행 시 DB 저장 후 삭제
    public PaymentResultVO payment(PaymentSO paymentSO) {
        // 카드번호 입력 및 중복 체크
        cardNumberService.checkUsingCardNumber(paymentSO.getCardNumber());
        try {
            // 결제 진행
            return paymentService.payment(paymentSO);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage(), e.getStatus());
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        } finally {
            // 카드번호 제거
            cardNumberService.deleteUsedCardNumber(paymentSO.getCardNumber());
        }
    }
}
