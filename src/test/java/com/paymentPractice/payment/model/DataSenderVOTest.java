package com.paymentPractice.payment.model;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class DataSenderVOTest {
    @Test
    void DataSenderVOTest() {
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber("123412341234")
                .installmentMonths(0)
                .expirationPeriod("0324")
                .cvc(123)
                .amount(777777)
                .vat(111)
                .originalManagementNumber("abcdeffddfasdfasdf")
                .encryptedCardInformation("eqwersdfgcxzv")
                .spareField("none")
                .build();

        String senderString = sender.getStringData();
        log.info("DataSenderVOTest : {}", senderString);
        Assertions.assertThat(senderString.length()).isEqualTo(416);

    }
}
