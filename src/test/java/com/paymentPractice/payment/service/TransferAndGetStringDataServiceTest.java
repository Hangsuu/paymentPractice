package com.paymentPractice.payment.service;

import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.entity.YesOrNo;
import com.paymentPractice.payment.model.PaymentVO;
import com.paymentPractice.testHelper.TransactionalTestService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@Slf4j
@SpringBootTest
public class TransferAndGetStringDataServiceTest {
    @Autowired
    SavePaymentService savePaymentService;
    @Autowired
    TransferAndGetStringDataService transferAndGetStringDataService;
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

    @Test
    void paymentSendingDataService() {
        PaymentVO paymentVO = new PaymentVO();
        paymentVO.setUserId(UUIDUserID);
        paymentVO.setEncryptedCardInformation("RVqimaWps0XiZ99wbqk1mUi6i95nhkNScogC8m5Kxn0");
        paymentVO.setInstallmentMonths(12);
        paymentVO.setAmount(10000);
        paymentVO.setCalculatedVat(1000);
        paymentVO.setVatDefaultYn(YesOrNo.Y);

        String resultAmountId = savePaymentService.savePaymentResult(paymentVO);

        paymentVO.setCardNumber("1111222233334444");
        paymentVO.setCvc("123");
        paymentVO.setExpirationPeriod("0499");

        String sendingString = transferAndGetStringDataService.paymentSendingData(resultAmountId, paymentVO);

        log.info("sendingString : {}", sendingString);
        Assertions.assertThat(sendingString).contains(resultAmountId);
        Assertions.assertThat(sendingString).contains("1111222233334444");
        Assertions.assertThat(sendingString).contains("123");
        Assertions.assertThat(sendingString).contains("0499");
    }

    @Test
    void paymentCancellationSendingDataTest() {
        PaymentVO paymentVO = new PaymentVO();
        paymentVO.setUserId(UUIDUserID);
        paymentVO.setEncryptedCardInformation("RVqimaWps0XiZ99wbqk1mUi6i95nhkNScogC8m5Kxn0");
        paymentVO.setInstallmentMonths(12);
        paymentVO.setAmount(10000);
        paymentVO.setCalculatedVat(1000);
        paymentVO.setVatDefaultYn(YesOrNo.Y);

        String resultAmountId = savePaymentService.savePaymentResult(paymentVO);

        PaymentEntity paymentEntity = transactionalTestService.getCancelPaymentEntity(resultAmountId);
        PaymentVO cancelPaymentVO = new PaymentVO();
        cancelPaymentVO.setUserId(UUIDUserID);
        cancelPaymentVO.setEncryptedCardInformation("RVqimaWps0XiZ99wbqk1mUi6i95nhkNScogC8m5Kxn0");
        cancelPaymentVO.setInstallmentMonths(12);
        cancelPaymentVO.setAmount(1000);
        cancelPaymentVO.setCalculatedVat(100);
        cancelPaymentVO.setVatDefaultYn(YesOrNo.Y);

        String resultCancelAmountId = savePaymentService.savePaymentCancellation(paymentEntity);

        String sendingString = transferAndGetStringDataService.paymentCancellationSendingData(resultCancelAmountId, paymentEntity);

        log.info("sendingString : {}", sendingString);
        Assertions.assertThat(sendingString).contains(resultAmountId);
        Assertions.assertThat(sendingString).contains("111122*******444");
        Assertions.assertThat(sendingString).contains("123");
        Assertions.assertThat(sendingString).contains("0499");
    }
}
