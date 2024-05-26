package com.paymentPractice.payment.model;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartialCancellationSO {
    // 관리번호
    private String amountId;
    // 취소금액
    private int amount;
    // 취소 부가가치세
    private Integer vat;

    public void cancellationValidationCheck() {
        // 금액 범위
        if (this.amount < 100 || this.amount > 1000000000) {
            throw new CustomException(ErrorCode.WRONG_AMOUNT);
        }
        // 부가가치세가 금액보다 크게 설정된 경우
        if (this.vat != null
                && (this.vat > this.amount || this.vat < 0)) {
            throw new CustomException(ErrorCode.WRONG_VAT);
        }
    }

}
