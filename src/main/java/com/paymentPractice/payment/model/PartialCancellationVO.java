package com.paymentPractice.payment.model;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import com.paymentPractice.payment.entity.YesOrNo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartialCancellationVO extends CalculatedVatVO{

    public PartialCancellationVO(PartialCancellationSO so) {
        so.partialCancellationValidationCheck();
        this.amountId = so.getAmountId();
        this.amount = so.getAmount();
        this.vat = so.getVat();
        vatCalculate(this.vat, this.amount);
    }

    // 관리번호
    private String amountId;
    // 취소금액
    private int amount;
    // 취소 부가가치세
    private Integer vat;

    public void partialCancellationAvailableCheck(int restAmount, int restVat) {
        // 취소 금액이 잔여금액보다 큰 경우
        if (restAmount < this.amount) {
            throw new CustomException(ErrorCode.EXCEED_REST_AMOUNT);
        }
        // 취소 부가가치세가 잔여 부가가치세보다 큰 경우
        if (this.vat != null
                && restVat < this.vat) {
            throw new CustomException(ErrorCode.EXCEED_REST_VAT);
        }
        // 잔여 부가가치세가 남는 경우
        if (restAmount == this.amount
                && this.vat != null
                && restVat - this.vat > 0) {
            throw new CustomException(ErrorCode.EXIST_REST_VAT);
        }
    }
}
