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
public class SavePaymentServiceTest {
    @Autowired
    SavePaymentService savePaymentService;
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
    void savePaymentResultTest() {
        PaymentVO paymentVO = new PaymentVO();
        paymentVO.setUserId(UUIDUserID);
        paymentVO.setEncryptedCardInformation("testCardInformation");
        paymentVO.setInstallmentMonths(12);
        paymentVO.setAmount(10000);
        paymentVO.setCalculatedVat(1000);
        paymentVO.setVatDefaultYn(YesOrNo.Y);

        String resultAmountId = savePaymentService.savePaymentResult(paymentVO);
        log.info("result amount id : {}", resultAmountId);
        int paymentListSizeByUserId = transactionalTestService.getPaymentListSizeByUserId(UUIDUserID);
        Assertions.assertThat(paymentListSizeByUserId).isEqualTo(1);
    }

    @Test
    void savePaymentCancellationTest() {
        PaymentVO paymentVO = new PaymentVO();
        paymentVO.setUserId(UUIDUserID);
        paymentVO.setEncryptedCardInformation("testCardInformation");
        paymentVO.setInstallmentMonths(12);
        paymentVO.setAmount(10000);
        paymentVO.setCalculatedVat(1000);
        paymentVO.setVatDefaultYn(YesOrNo.Y);

        String resultAmountId = savePaymentService.savePaymentResult(paymentVO);

        Assertions.assertThat(transactionalTestService.getCancelPaymentCount(resultAmountId)).isEqualTo(1);
    }

    @Test
    void savePartialCancellationTest() {
        PaymentVO paymentVO = new PaymentVO();
        paymentVO.setUserId(UUIDUserID);
        paymentVO.setEncryptedCardInformation("testCardInformation");
        paymentVO.setInstallmentMonths(12);
        paymentVO.setAmount(10000);
        paymentVO.setCalculatedVat(1000);
        paymentVO.setVatDefaultYn(YesOrNo.Y);

        String resultAmountId = savePaymentService.savePaymentResult(paymentVO);

        PaymentEntity paymentEntity = transactionalTestService.getCancelPaymentEntity(resultAmountId);
        PaymentVO cancelPaymentVO = new PaymentVO();
        cancelPaymentVO.setUserId(UUIDUserID);
        cancelPaymentVO.setEncryptedCardInformation("testCardInformation");
        cancelPaymentVO.setInstallmentMonths(12);
        cancelPaymentVO.setAmount(1000);
        cancelPaymentVO.setCalculatedVat(100);
        cancelPaymentVO.setVatDefaultYn(YesOrNo.Y);

        String resultCancelAmountId = savePaymentService.savePaymentCancellation(paymentEntity);

        Assertions.assertThat(transactionalTestService.getCancelAmountCount(resultCancelAmountId)).isEqualTo(1);
    }
}
