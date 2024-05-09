package com.paymentPractice.payment.service;

import com.paymentPractice.payment.model.PaymentSO;
import com.paymentPractice.testHelper.TransactionalTestService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class PaymentCardCheckServiceTest {
    @Autowired
    PaymentCardCheckService paymentCardCheckService;
    @Autowired
    TransactionalTestService transactionalTestService;

    private static String UUIDUserID = "";

    @BeforeAll
    static void setUUIDUserId() {
        UUIDUserID = UUID.randomUUID().toString().substring(0, 20);
    }

    @AfterEach
    void deleteData() {
        transactionalTestService.deleteByUserId(UUIDUserID);
    }

    private PaymentSO setBasicPaymentSO() {
        PaymentSO paymentSO = new PaymentSO();
        paymentSO.setCardNumber("1234123412341234");
        paymentSO.setExpirationPeriod("0499");
        paymentSO.setCvc("777");
        paymentSO.setInstallmentMonths(3);
        paymentSO.setAmount(100000);
        paymentSO.setVat(null);
        paymentSO.setUserId(UUIDUserID);
        return paymentSO;
    }

    @Test
    @DisplayName("결제 카드번호 동시성 테스트")
    void paymentTest() throws InterruptedException {
        log.info("PaymentCardCheckServiceTest.paymentTest start");
        Runnable task = () -> {
            PaymentSO paymentSO = setBasicPaymentSO();
            paymentCardCheckService.payment(paymentSO);
        };

        // 여러 스레드에서 동시에 실행되도록 함
        int threadCount = 2;
        Thread[] threads = new Thread[threadCount];

        for (int i=0; i<threadCount; i++) {
            threads[i] = new Thread(task);
        }

        // 스레드 시작
        for (Thread thread : threads) {
            thread.start();
        }

        // 모든 스레드가 종료될 때까지 대기
        for (Thread thread : threads) {
            thread.join();
        }

        // 1건만 처리 확인
        assertThat(transactionalTestService.getPaymentNumberByUserId(UUIDUserID)).isEqualTo(1);
    }
}