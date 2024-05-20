package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import com.paymentPractice.payment.entity.AmountType;
import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.model.CardInformationVO;
import com.paymentPractice.payment.model.CommonHeaderVO;
import com.paymentPractice.payment.model.DataSenderVO;
import com.paymentPractice.payment.model.PaymentVO;
import com.paymentPractice.payment.service.CardApiService;
import com.paymentPractice.payment.service.CardInformationConversionService;
import com.paymentPractice.payment.service.TransferAndGetStringDataService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class TransferAndGetStringDataServiceImpl implements TransferAndGetStringDataService {
    private final CardApiService cardApiService;
    private final CardInformationConversionService cardInformationConversionService;

    @Override
    public String paymentSendingData(String amountId, PaymentVO paymentVO) {
        CommonHeaderVO header = CommonHeaderVO.builder()
                .dataDivision(String.valueOf(AmountType.PAYMENT))
                .managementNumber(amountId)
                .build();
        header.setTotalDataLength();
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber(paymentVO.getCardNumber())
                .installmentMonths(paymentVO.getInstallmentMonths())
                .expirationPeriod(paymentVO.getExpirationPeriod())
                .cvc(Integer.parseInt(paymentVO.getCvc()))
                .amount(paymentVO.getAmount())
                .vat(paymentVO.getCalculatedVat())
                .originalManagementNumber("")
                .encryptedCardInformation(paymentVO.getEncryptedCardInformation())
                .spareField(paymentVO.getUserId())
                .build();

        return getAndSendStringData(header, sender);
    }

    @Override
    public String paymentCancellationSendingData(String amountId, PaymentEntity paymentEntity) {
        CardInformationVO cardInformationVO = cardInformationConversionService.getCardInformation(paymentEntity.getCardInformation());

        CommonHeaderVO header = CommonHeaderVO.builder()
                .dataDivision(String.valueOf(AmountType.CANCEL))
                .managementNumber(amountId)
                .build();
        header.setTotalDataLength();
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber(cardInformationVO.getCardNumber())
                .installmentMonths(paymentEntity.getInstallmentMonths())
                .expirationPeriod(cardInformationVO.getExpirationPeriod())
                .cvc(Integer.parseInt(cardInformationVO.getCvc()))
                .amount(paymentEntity.getRestAmount())
                .vat(paymentEntity.getRestVat())
                .originalManagementNumber(paymentEntity.getFirstPaymentAmountId())
                .encryptedCardInformation(paymentEntity.getCardInformation())
                .spareField(paymentEntity.getUserId())
                .build();
        return getAndSendStringData(header, sender);
    }

    // String data 생성 및 전송
    private String getAndSendStringData(CommonHeaderVO header, DataSenderVO sender) {
        String stringData = header.getStringData() + sender.getStringData();
        // String data 전송
        boolean sendDateSuccess = cardApiService.sendHttpToCardApi(stringData);
        if(!sendDateSuccess) {
            throw new CustomException(ErrorCode.TRANSFER_DATA_FAIL);
        }
        return stringData;
    }
}
