package com.paymentPractice.payment.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PartialCancellationSOTest {
    @Test
    void cancellationAmountValidationCheckTest() {
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmount(99);
        partialCancellationSO.setVat(null);
        Assertions.assertThatThrownBy(()-> {
            partialCancellationSO.cancellationValidationCheck();
        });
        partialCancellationSO.setAmount(1000000001);
        Assertions.assertThatThrownBy(()-> {
            partialCancellationSO.cancellationValidationCheck();
        });
        partialCancellationSO.setAmount(999999999);
        partialCancellationSO.cancellationValidationCheck();
    }

    @Test
    void cancellationVatValidationCheckTest() {
        PartialCancellationSO partialCancellationSO = new PartialCancellationSO();
        partialCancellationSO.setAmount(1000);
        partialCancellationSO.setVat(1001);
        Assertions.assertThatThrownBy(()-> {
            partialCancellationSO.cancellationValidationCheck();
        });
        partialCancellationSO.setVat(-1);
        Assertions.assertThatThrownBy(()-> {
            partialCancellationSO.cancellationValidationCheck();
        });
        partialCancellationSO.setVat(null);
        partialCancellationSO.cancellationValidationCheck();
        partialCancellationSO.setVat(999);
        partialCancellationSO.cancellationValidationCheck();
    }
}
