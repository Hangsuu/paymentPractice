package com.paymentPractice.payment.model;

import com.paymentPractice.payment.entity.YesOrNo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartialCancellationVO extends CalculatedVatVO{

    public PartialCancellationVO(PartialCancellationSO so) {
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
}
