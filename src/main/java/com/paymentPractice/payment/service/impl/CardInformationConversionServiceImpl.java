package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.service.TwoWayEncryptionService;
import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.model.CardInformationVO;
import com.paymentPractice.payment.model.PaymentSO;
import com.paymentPractice.payment.model.PaymentVO;
import com.paymentPractice.payment.service.CardInformationConversionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CardInformationConversionServiceImpl implements CardInformationConversionService {
    private final TwoWayEncryptionService twoWayEncryptionService;

    @Override
    public void getEncryptedCardInformation(PaymentVO paymentVO) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(paymentVO.getCardNumber());
        stringBuffer.append("/");
        stringBuffer.append(paymentVO.getExpirationPeriod());
        stringBuffer.append("/");
        stringBuffer.append(paymentVO.getCvc());

        String cardInformation = stringBuffer.toString();
        paymentVO.setEncryptedCardInformation(twoWayEncryptionService.encrypt(cardInformation));
    }

    @Override
    public CardInformationVO getCardInformation(PaymentEntity paymentEntity) {
        // 복호화 후 / 단위로 끊어 카드정보 반환
        String decryptedCardInformation = twoWayEncryptionService.decrypt(paymentEntity.getCardInformation());
        String[] cardInformations = decryptedCardInformation.split("/");
        CardInformationVO cardInformation = CardInformationVO.builder()
                .cardNumber(maskingCardNumber(cardInformations[0]))
                .expirationPeriod(cardInformations[1])
                .cvc(cardInformations[2]).build();
        return cardInformation;
    }

    private String maskingCardNumber(String cardNumber) {
        StringBuffer stringBuffer = new StringBuffer();
        String[] cardNumbers = cardNumber.split("");
        for(int i=0; i < cardNumbers.length; i++) {
            if(i < 6 || i > cardNumbers.length-4 ) {
                stringBuffer.append(cardNumbers[i]);
            } else {
                stringBuffer.append("*");
            }
        }
        return stringBuffer.toString();
    }
}
