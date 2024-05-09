package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import com.paymentPractice.payment.model.PartialCancellationSO;
import com.paymentPractice.payment.model.PaymentSO;
import com.paymentPractice.payment.service.ValidationCheckService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ValidationCheckServiceImpl implements ValidationCheckService {
    @Override
    public void paymentValidationCheck(PaymentSO paymentSO) {
        // 카드번호 10~16자리 여부
        if (!paymentSO.getCardNumber().matches("^\\d{10,16}$")) {
            // (400) 잘못된 형식의 카드번호입니다.
            throw new CustomException(ErrorCode.WRONG_CARD_NUMBER);
        }
        // 유효기간 형식 MMYY 준수 여부
        if (!paymentSO.getExpirationPeriod().matches("^(0[1-9]|1[0-2])\\d{2}$")) {
            // (400) 잘못된 형식의 유효기간입니다.
            throw new CustomException(ErrorCode.WRONG_EXPIRATION_PERIOD);
        }
        // cvc 숫자 판단
        if (!paymentSO.getCvc().matches("^\\d{3}$")) {
            // (400) 잘못된 형식의 CVC입니다.
            throw new CustomException(ErrorCode.WRONG_CVC);
        }
        // 할부개월 범위 0 ~ 12
        if (paymentSO.getInstallmentMonths() < 0 || paymentSO.getInstallmentMonths() > 12) {
            // (400) 잘못된 범위의 할부개월입니다.
            throw new CustomException(ErrorCode.WRONG_INSTALLMENT_MONTHS);
        }
        // 금액 범위 100 ~ 1,000,000,000
        if (paymentSO.getAmount() < 100 || paymentSO.getAmount() > 1000000000) {
            // (400) 잘못된 범위의 결제금액입니다.
            throw new CustomException(ErrorCode.WRONG_AMOUNT);
        }
        // 부가가치세가 금액범위보다 큰지 판단
        if (paymentSO.getVat() != null
                && (paymentSO.getVat() > paymentSO.getAmount() || paymentSO.getVat() < 0)) {
            // (400) 잘못된 범위의 부가가치세입니다.
            throw new CustomException(ErrorCode.WRONG_VAT);
        }
        // 만료되지 않은 카드인지 체크
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear() % 100;
        int expirationMonth = Integer.parseInt(paymentSO.getExpirationPeriod().substring(0, 2));
        int expirationYear = Integer.parseInt(paymentSO.getExpirationPeriod().substring(2));
        // 현재 연/월 보다 카드 유효기간이 이전이면 예외처리
        if (currentYear > expirationYear ||
                (currentYear == expirationYear && currentMonth > expirationMonth) ) {
            // (400) 유효기간이 만료된 카드입니다.
            throw new CustomException(ErrorCode.WRONG_EXPIRATION_DATE);
        }
    }

    @Override
    public void partialCancellationValidationCheck(PartialCancellationSO partialCancellationSO) {
        // 금액 범위
        if (partialCancellationSO.getAmount() < 100 || partialCancellationSO.getAmount() > 1000000000) {
            // (400) 잘못된 범위의 결제금액입니다.
            throw new CustomException(ErrorCode.WRONG_AMOUNT);
        }
        // 부가가치세가 금액보다 크게 설정된 경우
        if (partialCancellationSO.getVat() != null
                && (partialCancellationSO.getVat() > partialCancellationSO.getAmount() || partialCancellationSO.getVat() < 0)) {
            // (400) 잘못된 범위의 부가가치세입니다.
            throw new CustomException(ErrorCode.WRONG_VAT);
        }
    }
}
