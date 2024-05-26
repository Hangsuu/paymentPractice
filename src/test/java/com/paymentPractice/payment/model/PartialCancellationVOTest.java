package com.paymentPractice.payment.model;

import com.paymentPractice.common.exception.CustomException;
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
    }
}
