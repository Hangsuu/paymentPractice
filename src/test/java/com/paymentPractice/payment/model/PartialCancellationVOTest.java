package com.paymentPractice.payment.model;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.payment.entity.YesOrNo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PartialCancellationVOTest {
    @Test
    void partialCancellationRestAmountAvailableCheckTest() {
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmountId("testAmountID");
        partialCancellationSO.setVat(null);
        partialCancellationSO.setAmount(10000);

        PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
        Assertions.assertThatThrownBy(() -> {
            partialCancellationVO.partialCancellationAvailableCheck(100, 1000);
        }).isInstanceOf(CustomException.class);

        partialCancellationVO.partialCancellationAvailableCheck(15000, 1000);
    }
    @Test
    void partialCancellationVatAvailableCheckTest() {
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmountId("testAmountID");
        partialCancellationSO.setVat(1000);
        partialCancellationSO.setAmount(10000);

        PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
        Assertions.assertThatThrownBy(() -> {
            partialCancellationVO.partialCancellationAvailableCheck(100, 1500);
        }).isInstanceOf(CustomException.class);

        partialCancellationVO.partialCancellationAvailableCheck(12000, 1000);
    }

    @Test
    void partialCancellationRestVatAvailableCheckTest() {
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmountId("testAmountID");
        partialCancellationSO.setVat(1000);
        partialCancellationSO.setAmount(10000);

        PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
        Assertions.assertThatThrownBy(() -> {
            partialCancellationVO.partialCancellationAvailableCheck(10000, 900);
        }).isInstanceOf(CustomException.class);

        partialCancellationVO.partialCancellationAvailableCheck(10000, 1000);
    }

    @Test
    void partialCancellationVOConstructorAmountValidationCheckTest() {
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmountId("testAmountID");
        partialCancellationSO.setVat(null);
        partialCancellationSO.setAmount(99);

        Assertions.assertThatThrownBy(() -> {
            PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
        }).isInstanceOf(CustomException.class);

        partialCancellationSO.setAmount(1000000001);
        Assertions.assertThatThrownBy(() -> {
            PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
        }).isInstanceOf(CustomException.class);

        partialCancellationSO.setAmount(10000);
        PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
    }

    @Test
    void partialCancellationVOConstructorVatValidationCheckTest(){
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmountId("testAmountID");
        partialCancellationSO.setVat(10001);
        partialCancellationSO.setAmount(10000);

        Assertions.assertThatThrownBy(() -> {
            PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
        }).isInstanceOf(CustomException.class);

        partialCancellationSO.setVat(-1);

        Assertions.assertThatThrownBy(() -> {
            PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);
        }).isInstanceOf(CustomException.class);

        partialCancellationSO.setVat(50);
        PartialCancellationVO partialCancellationVO1 = new PartialCancellationVO(partialCancellationSO);

        partialCancellationSO.setVat(null);
        PartialCancellationVO partialCancellationVO2 = new PartialCancellationVO(partialCancellationSO);
    }

    @Test
    void partialCancellationVOConstructorVatCalculateTest() {
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmountId("testAmountID");
        partialCancellationSO.setVat(1000);
        partialCancellationSO.setAmount(10000);

        PartialCancellationVO partialCancellationVO1 = new PartialCancellationVO(partialCancellationSO);
        Assertions.assertThat(partialCancellationVO1.getCalculatedVat()).isEqualTo(1000);
        Assertions.assertThat(partialCancellationVO1.getVatDefaultYn()).isEqualTo(YesOrNo.N);

        partialCancellationSO.setVat(null);
        PartialCancellationVO partialCancellationVO2 = new PartialCancellationVO(partialCancellationSO);
        Assertions.assertThat(partialCancellationVO2.getCalculatedVat()).isEqualTo((int) Math.round((double) 10000 / 11.0));
        Assertions.assertThat(partialCancellationVO2.getVatDefaultYn()).isEqualTo(YesOrNo.Y);

        partialCancellationSO.setVat((int) Math.round((double) 10000 / 11.0));
        PartialCancellationVO partialCancellationVO3 = new PartialCancellationVO(partialCancellationSO);
        Assertions.assertThat(partialCancellationVO3.getCalculatedVat()).isEqualTo((int) Math.round((double) 10000 / 11.0));
        Assertions.assertThat(partialCancellationVO3.getVatDefaultYn()).isEqualTo(YesOrNo.N);
    }
}
