package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.aspect.MethodLog;
import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import com.paymentPractice.payment.entity.*;
import com.paymentPractice.payment.model.*;
import com.paymentPractice.payment.repository.AmountRepository;
import com.paymentPractice.payment.repository.PaymentRepository;
import com.paymentPractice.payment.service.CardInformationConversionService;
import com.paymentPractice.payment.service.PaymentService;
import com.paymentPractice.payment.service.TransferAndGetStringDataService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final AmountRepository amountRepository;
    private final CardInformationConversionService cardInformationConversionService;
    private final TransferAndGetStringDataService transferAndGetStringDataService;

    @Override
    @Transactional
    @MethodLog(description = "결제 메서드")
    public PaymentResultVO payment(PaymentSO paymentSO) {
        PaymentVO paymentVO = new PaymentVO(paymentSO);

        // 카드정보 암호화
        cardInformationConversionService.getEncryptedCardInformation(paymentVO);

        // 데이터 저장
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

        // String data 생성 및 전송
        String stringData = transferAndGetStringDataService.paymentSendingData(amountEntity.getAmountId(), paymentVO);

        return PaymentResultVO.builder()
                .amountId(amountEntity.getAmountId())
                .stringData(stringData).build();
    }

    @Override
    @Transactional
    @MethodLog(description = "전체 취소 메서드")
    public PaymentResultVO paymentCancellation(PaymentCancellationSO paymentCancellationSO) {
        // 올바른 amount id인지 판단 및 예외처리
        AmountEntity amountEntity = getAmountEntityById(paymentCancellationSO.getAmountId());

        // 해당 결제 정보 반환
        PaymentEntity paymentEntity = amountEntity.getPaymentEntityWithCancellationCheck();

        // 금액 데이터 저장
        AmountEntity cancelAmount = AmountEntity.builder()
                .amount(paymentEntity.getRestAmount())
                .paymentEntity(paymentEntity)
                .amountType(AmountType.CANCEL)
                .vat(paymentEntity.getRestVat())
                .vatDefaultYn(YesOrNo.N)
                .build();
        cancelAmount.setAmountInsertData();
        amountRepository.save(cancelAmount);

        // 결제상태, 할부개월수 데이터 저장
        paymentEntity.setCancellation();

        // String data 생성 및 전송
        String stringData = transferAndGetStringDataService.paymentCancellationSendingData(amountEntity.getAmountId(), paymentEntity);

        return PaymentResultVO.builder()
                .amountId(cancelAmount.getAmountId())
                .stringData(stringData).build();
    }

    @Override
    @Transactional(readOnly = true)
    @MethodLog(description = "결제정보 조회 메서드")
    public PaymentInformationVO paymentInformation(PaymentInformationSO paymentInformationSO) {
        // 올바른 amount id인지 판단 및 예외처리
        AmountEntity amountEntity = getAmountEntityById(paymentInformationSO.getAmountId());
        // 해당 결제 정보 반환
        PaymentEntity paymentEntity = amountEntity.getPaymentEntity();

        // 카드정보 복호화 (카드번호/만료일자/cvc)
        CardInformationVO cardInformation = cardInformationConversionService.getCardInformation(paymentEntity);

        return PaymentInformationVO.builder()
                .amountId(amountEntity.getAmountId())
                .cardNumber(cardInformation.getCardNumber())
                .expirationPeriod(cardInformation.getExpirationPeriod())
                .cvc(cardInformation.getCvc())
                .amountType(amountEntity.getAmountType())
                .amount(amountEntity.getAmount())
                .vat(amountEntity.getVat()).build();
    }

    @Override
    @Transactional
    @MethodLog(description = "결제 부분취소 메서드")
    public PaymentResultVO partialCancellation(PartialCancellationSO partialCancellationSO) {
        // 부분취소 유효성 체크
        PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);

        // 올바른 amount id인지 판단 및 예외처리
        AmountEntity amountEntity = getAmountEntityById(partialCancellationSO.getAmountId());

        // 해당 결제 정보 반환
        PaymentEntity paymentEntity = amountEntity.getPaymentEntityWithCancellationCheck();

        // 취소 가능 여부 판단
        partialCancellationSO.partialCancellationAvailableCheck(paymentEntity.getRestAmount(), paymentEntity.getRestVat());

        // 금액 데이터 저장
        AmountEntity cancelAmount = AmountEntity.builder()
                .amount(partialCancellationVO.getAmount())
                .paymentEntity(paymentEntity)
                .amountType(AmountType.CANCEL)
                .vat(partialCancellationVO.getCalculatedVat())
                .vatDefaultYn(partialCancellationVO.getVatDefaultYn())
                .build();
        cancelAmount.setAmountInsertData();
        amountRepository.save(cancelAmount);

        paymentEntity.setPartialCancellation(partialCancellationVO);

        // String data 생성 및 전송
        String stringData = transferAndGetStringDataService.paymentCancellationSendingData(amountEntity.getAmountId(), paymentEntity);

        return PaymentResultVO.builder()
                .amountId(cancelAmount.getAmountId())
                .stringData(stringData).build();
    }

    // 올바른 amount id인지 판단 및 예외처리
    private AmountEntity getAmountEntityById(String amountId) {
        AmountEntity amountEntity = amountRepository.findById(amountId)
                .orElseThrow(() -> {
                    throw new CustomException(ErrorCode.WRONG_AMOUNT_ID);
                });
        return amountEntity;
    }

}
