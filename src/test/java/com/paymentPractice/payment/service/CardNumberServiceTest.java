package com.paymentPractice.payment.service;

import com.paymentPractice.payment.repository.CardNumberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class CardNumberServiceTest {
    @Autowired
    CardNumberService cardNumberService;
    @Autowired
    CardNumberRepository cardNumberRepository;

    @Test
    @DisplayName("카드번호 입력 동시성 테스트")
    void checkUsingCardNumberTest() throws InterruptedException {
        log.info("CardNumberServiceTest.checkUsingCardNumberTest start");

        Runnable task = () -> {
            cardNumberService.checkUsingCardNumber("0000000000000000");
        };

        // 여러 스레드에서 동시에 실행되도록 함
        Thread[] threads = new Thread[2];
        threads[0] = new Thread(task);
        threads[1] = new Thread(task);

        // 스레드 시작
        threads[0].start();
        threads[1].start();

        // 모든 스레드가 종료될 때까지 대기
        threads[0].join();
        threads[1].join();

        assertThat(cardNumberRepository.findById("0000000000000000").isPresent());
        cardNumberService.deleteUsedCardNumber("0000000000000000");
    }

    @Test
    @DisplayName("카드번호 제거 테스트")
    void deleteUsedCardNumber(){
        log.info("CardNumberServiceTest.deleteUsedCardNumber start");

        cardNumberService.checkUsingCardNumber("0000000000000000");

        cardNumberService.deleteUsedCardNumber("0000000000000000");
        assertThat(cardNumberRepository.findById("0000000000000000").isEmpty());
    }
}
