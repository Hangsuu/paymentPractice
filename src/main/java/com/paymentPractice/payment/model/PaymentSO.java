package com.paymentPractice.payment.model;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import com.paymentPractice.payment.entity.YesOrNo;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentSO {
    // 카드 번호
    private String cardNumber;
    // 유효기간
    private String expirationPeriod;
    // cvc
    private String cvc;
    // 할부개월수
    private int installmentMonths;
    // 결제금액
    private int amount;
    // 부가가치세
    private Integer vat;
    // 유저이름
    private String userId;

    public void paymentValidationCheck() {
        // 카드번호 10~16자리 여부
        if (!this.cardNumber.matches("^\\d{10,16}$")) {
            throw new CustomException(ErrorCode.WRONG_CARD_NUMBER);
        }
        // 유효기간 형식 MMYY 준수 여부
        if (!this.expirationPeriod.matches("^(0[1-9]|1[0-2])\\d{2}$")) {
            throw new CustomException(ErrorCode.WRONG_EXPIRATION_PERIOD);
        }
        // cvc 숫자 판단
        if (!this.cvc.matches("^\\d{3}$")) {
            throw new CustomException(ErrorCode.WRONG_CVC);
        }
        // 할부개월 범위 0 ~ 12
        if (this.installmentMonths < 0 || this.installmentMonths > 12) {
            throw new CustomException(ErrorCode.WRONG_INSTALLMENT_MONTHS);
        }
        // 금액 범위 100 ~ 1,000,000,000
        if (this.amount < 100 || this.amount > 1000000000) {
            throw new CustomException(ErrorCode.WRONG_AMOUNT);
        }
        // 부가가치세가 금액범위보다 큰지 판단
        if (this.vat != null
                && (this.vat > this.amount || this.vat < 0)) {
            throw new CustomException(ErrorCode.WRONG_VAT);
        }
        // 만료되지 않은 카드인지 체크
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear() % 100;
        int expirationMonth = Integer.parseInt(this.expirationPeriod.substring(0, 2));
        int expirationYear = Integer.parseInt(this.expirationPeriod.substring(2));
        // 현재 연/월 보다 카드 유효기간이 이전이면 예외처리
        if (currentYear > expirationYear ||
                (currentYear == expirationYear && currentMonth > expirationMonth) ) {
            throw new CustomException(ErrorCode.WRONG_EXPIRATION_DATE);
        }
    }
}
