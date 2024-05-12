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

    public void partialCancellationValidationCheck() {
        // 금액 범위
        if (this.getAmount() < 100 || this.getAmount() > 1000000000) {
            throw new CustomException(ErrorCode.WRONG_AMOUNT);
        }
        // 부가가치세가 금액보다 크게 설정된 경우
        if (this.getVat() != null
                && (this.getVat() > this.getAmount() || this.getVat() < 0)) {
            throw new CustomException(ErrorCode.WRONG_VAT);
        }
    }
}
