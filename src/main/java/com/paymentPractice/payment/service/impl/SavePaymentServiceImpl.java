package com.paymentPractice.payment.service.impl;

import com.paymentPractice.payment.entity.AmountEntity;
import com.paymentPractice.payment.entity.AmountType;
import com.paymentPractice.payment.entity.PaymentEntity;
import com.paymentPractice.payment.entity.PaymentStatus;
import com.paymentPractice.payment.model.PaymentVO;
import com.paymentPractice.payment.repository.AmountRepository;
import com.paymentPractice.payment.repository.PaymentRepository;
import com.paymentPractice.payment.service.SavePaymentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class SavePaymentServiceImpl implements SavePaymentService {
    private final PaymentRepository paymentRepository;
    private final AmountRepository amountRepository;

    @Override
    public String savePaymentResult(PaymentVO paymentVO) {
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .userId(paymentVO.getUserId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .cardInformation(paymentVO.getEncryptedCardInformation())
                .installmentMonths(paymentVO.getInstallmentMonths())
                .build();
        paymentEntity.setPaymentInsertData();
        paymentRepository.save(paymentEntity);
        AmountEntity amountEntity = AmountEntity.builder()
                .amount(paymentVO.getAmount())
                .paymentEntity(paymentEntity)
                .amountType(AmountType.PAYMENT)
                .vat(paymentVO.getCalculatedVat())
                .vatDefaultYn(paymentVO.getVatDefaultYn())
                .build();
        amountEntity.setAmountInsertData();
        amountRepository.save(amountEntity);

        return amountEntity.getAmountId();
    }
}
