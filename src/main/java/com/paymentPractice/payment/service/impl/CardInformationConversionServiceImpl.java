package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.service.TwoWayEncryptionService;
import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.model.CardInformationVO;
import com.paymentPractice.payment.model.PaymentSO;
import com.paymentPractice.payment.service.CardInformationConversionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CardInformationConversionServiceImpl implements CardInformationConversionService {
    private final TwoWayEncryptionService twoWayEncryptionService;

    @Override
    public String getEncryptedCardInformation(PaymentSO paymentSO) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(paymentSO.getCardNumber());
        stringBuffer.append("/");
        stringBuffer.append(paymentSO.getExpirationPeriod());
        stringBuffer.append("/");
        stringBuffer.append(paymentSO.getCvc());

        String cardInformation = stringBuffer.toString();
        String encryptedCardInformation = twoWayEncryptionService.encrypt(cardInformation);
        return encryptedCardInformation;
    }

    @Override
    public CardInformationVO getCardInformation(PaymentEntity paymentEntity) {
        // 복호화 후 / 단위로 끊어 카드정보 반환
        String decryptedCardInformation = twoWayEncryptionService.decrypt(paymentEntity.getCardInformation());
        String[] cardInformations = decryptedCardInformation.split("/");
        CardInformationVO cardInformation = CardInformationVO.builder()
                .cardNumber(cardInformations[0])
                .expirationPeriod(cardInformations[1])
                .cvc(cardInformations[2]).build();
        return cardInformation;
    }

    @Override
    public String maskingCardNumber(String cardNumber) {
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
