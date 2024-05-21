package com.paymentPractice.payment.service;

import com.paymentPractice.common.service.TwoWayEncryptionService;
import com.paymentPractice.payment.model.CardInformationVO;
import com.paymentPractice.payment.model.PaymentVO;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class CardInformationConversionServiceTest {
    @Autowired
    TwoWayEncryptionService twoWayEncryptionService;
    @Autowired
    CardInformationConversionService cardInformationConversionService;

    @Test
    void getEncryptedCardInformation() {
        PaymentVO paymentVO = new PaymentVO();
        paymentVO.setCardNumber("1111222233334444");
        paymentVO.setCvc("123");
        paymentVO.setExpirationPeriod("0499");

        Assertions.assertThat(paymentVO.getEncryptedCardInformation()).isNull();

        cardInformationConversionService.getEncryptedCardInformation(paymentVO);

        Assertions.assertThat(paymentVO.getEncryptedCardInformation()).isNotNull();
        log.info("encrypted card information : {}", paymentVO.getEncryptedCardInformation());
    }

    @Test
    void getCardInformationTest() {
        String encryptedCardInformation = "RVqimaWps0XiZ99wbqk1mUi6i95nhkNScogC8m5Kxn0=";
        CardInformationVO cardInformationVO = cardInformationConversionService.getCardInformation(encryptedCardInformation);
        Assertions.assertThat(cardInformationVO.getCardNumber()).isNotEqualTo("1111222233334444");
        Assertions.assertThat(cardInformationVO.getCardNumber()).isEqualTo("111122*******444");
        Assertions.assertThat(cardInformationVO.getCvc()).isEqualTo("123");
        Assertions.assertThat(cardInformationVO.getExpirationPeriod()).isEqualTo("0499");
    }

}
