package com.paymentPractice.payment.model;

import com.paymentPractice.payment.entity.AmountType;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class CommonHeaderVOTest {
    @Test
    void commonHeaderVOTest() {
        CommonHeaderVO header = CommonHeaderVO.builder()
                .dataLength(446)
                .dataDivision(String.valueOf(AmountType.CANCEL))
                .managementNumber("123124AASD")
                .build();

        String headerString = header.getStringData();
        log.info("CommonHeaderVOTest : {}", headerString);

        Assertions.assertThat(headerString.length()).isEqualTo(34);
    }
}
