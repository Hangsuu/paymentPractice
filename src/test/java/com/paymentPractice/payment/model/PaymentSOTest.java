package com.paymentPractice.payment.model;

import com.paymentPractice.common.exception.CustomException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

public class PaymentSOTest {
    @Test
    void cardNumberValidationCheckTest() {
        PaymentSO paymentSO = setDefaultPaymentSO();

        paymentSO.setCardNumber("123456789");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setCardNumber("123456789a");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setCardNumber("12345678901234567");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setCardNumber("1234567890");
    }

    @Test
    void expirationPeriodValidationCheckTest() {
        PaymentSO paymentSO = setDefaultPaymentSO();

        paymentSO.setExpirationPeriod("0000");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setExpirationPeriod("a123");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setExpirationPeriod("0199");
        paymentSO.paymentValidationCheck();

        paymentSO.setExpirationPeriod("1299");
        paymentSO.paymentValidationCheck();
    }

    @Test
    void cvcValidationCheckTest() {
        PaymentSO paymentSO = setDefaultPaymentSO();

        paymentSO.setCvc("11");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setCvc("1111");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setCvc("111");
        paymentSO.paymentValidationCheck();
    }

    @Test
    void installmentMonthsValidationCheckTest() {
        PaymentSO paymentSO = setDefaultPaymentSO();

        paymentSO.setInstallmentMonths(-1);
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setInstallmentMonths(13);
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setInstallmentMonths(0);
        paymentSO.paymentValidationCheck();

        paymentSO.setInstallmentMonths(12);
        paymentSO.paymentValidationCheck();
    }

    @Test
    void amountValidationCheckTest() {
        PaymentSO paymentSO = setDefaultPaymentSO();

        paymentSO.setAmount(99);
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setAmount(1000000001);
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setAmount(100);
        paymentSO.paymentValidationCheck();

        paymentSO.setAmount(999999999);
        paymentSO.paymentValidationCheck();
    }

    @Test
    void vatValidationCheckTest() {
        PaymentSO paymentSO = setDefaultPaymentSO();

        paymentSO.setAmount(100);

        paymentSO.setVat(101);
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setVat(-1);
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        paymentSO.setVat(100);
        paymentSO.paymentValidationCheck();

        paymentSO.setVat(null);
        paymentSO.paymentValidationCheck();
    }

    @Test
    void expiredValidationCheckTest() {
        PaymentSO paymentSO = setDefaultPaymentSO();

        paymentSO.setExpirationPeriod("0123");
        Assertions.assertThatThrownBy(() -> {
            paymentSO.paymentValidationCheck();
        }).isInstanceOf(CustomException.class);

        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue();
        int currentYear = currentDate.getYear() % 100;

        paymentSO.setExpirationPeriod(String.format("%02d", currentMonth) + String.format("%02d", currentYear));
    }

    private PaymentSO setDefaultPaymentSO() {
        PaymentSO paymentSO = new PaymentSO();
        paymentSO.setCardNumber("1234567890");
        paymentSO.setExpirationPeriod("1299");
        paymentSO.setCvc("123");
        paymentSO.setInstallmentMonths(0);
        paymentSO.setAmount(100);
        paymentSO.setVat(null);

        return paymentSO;
    }
}
