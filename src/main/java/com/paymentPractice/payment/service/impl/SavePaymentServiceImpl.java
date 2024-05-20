package com.paymentPractice.payment.service.impl;

import com.paymentPractice.payment.entity.*;
import com.paymentPractice.payment.model.PartialCancellationVO;
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
        paymentEntity.setPaymentDataBeforeInsert();
        paymentRepository.save(paymentEntity);
        AmountEntity amountEntity = AmountEntity.builder()
                .amount(paymentVO.getAmount())
                .paymentEntity(paymentEntity)
                .amountType(AmountType.PAYMENT)
                .vat(paymentVO.getCalculatedVat())
                .vatDefaultYn(paymentVO.getVatDefaultYn())
                .build();
        amountEntity.setAmountDataBeforeInsert();
        amountRepository.save(amountEntity);

        return amountEntity.getAmountId();
    }

    @Override
    public String savePaymentCancellation(PaymentEntity paymentEntity) {
        // 금액 데이터 저장
        AmountEntity cancelAmount = AmountEntity.builder()
                .amount(paymentEntity.getRestAmount())
                .paymentEntity(paymentEntity)
                .amountType(AmountType.CANCEL)
                .vat(paymentEntity.getRestVat())
                .vatDefaultYn(YesOrNo.N)
                .build();
        cancelAmount.setAmountDataBeforeInsert();
        amountRepository.save(cancelAmount);
        return cancelAmount.getAmountId();
    }

    @Override
    public String savePartialCancellation(PartialCancellationVO partialCancellationVO, PaymentEntity paymentEntity) {
        // 금액 데이터 저장
        AmountEntity cancelAmount = AmountEntity.builder()
                .amount(partialCancellationVO.getAmount())
                .paymentEntity(paymentEntity)
                .amountType(AmountType.CANCEL)
                .vat(partialCancellationVO.getCalculatedVat())
                .vatDefaultYn(partialCancellationVO.getVatDefaultYn())
                .build();
        cancelAmount.setAmountDataBeforeInsert();
        amountRepository.save(cancelAmount);

        paymentEntity.setPartialCancellation(partialCancellationVO);

        return cancelAmount.getAmountId();
    }
}
