package com.paymentPractice.payment.model;

import com.paymentPractice.payment.entity.YesOrNo;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CalculatedVatVO {

    private String encryptedCardInformation;
    private int calculatedVat;
    private YesOrNo vatDefaultYn;

    // 부가가치세 설정
    protected void vatCalculate(Integer vat, int amount) {
        int vatTemp = 0;
        YesOrNo vatDefaultYnTemp = null;
        // 부가가치세를 null로 받아 /11 을 적용받는 경우
        if (vat == null) {
            vatTemp = (int) Math.round((double) amount / 11.0);
            vatDefaultYnTemp = YesOrNo.Y;
        } else {
            vatTemp = vat;
            vatDefaultYnTemp = YesOrNo.N;
        }
        this.calculatedVat = vatTemp;
        this.vatDefaultYn = vatDefaultYnTemp;
    }
}
