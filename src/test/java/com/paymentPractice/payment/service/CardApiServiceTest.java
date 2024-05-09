package com.paymentPractice.payment.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class CardApiServiceTest {
    @Autowired
    CardApiService cardApiService;

    @Test
    void sendHttpToCardApiTest() {
        String wrongData = "입력용 테스트 데이터";
        assertThat(cardApiService.sendHttpToCardApi(wrongData)).isTrue();
    }
}
