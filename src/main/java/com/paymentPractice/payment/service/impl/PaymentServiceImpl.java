package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.aspect.MethodLog;
import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import com.paymentPractice.common.service.TwoWayEncryptionService;
import com.paymentPractice.payment.entity.*;
import com.paymentPractice.payment.model.*;
import com.paymentPractice.payment.repository.AmountRepository;
import com.paymentPractice.payment.repository.PaymentRepository;
import com.paymentPractice.payment.service.CardApiService;
import com.paymentPractice.payment.service.CardInformationConversionService;
import com.paymentPractice.payment.service.PaymentService;
import com.paymentPractice.payment.service.ValidationCheckService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final AmountRepository amountRepository;
    private final CardApiService cardApiService;
    private final CardInformationConversionService cardInformationConversionService;

    @Override
    @Transactional
    @MethodLog(description = "결제 메서드")
    public PaymentResultVO payment(PaymentSO paymentSO) {
        // 결제 유효성 체크
        paymentSO.paymentValidationCheck();

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

        // 전달할 String data 헤더, 데이터 객체 생성
        CommonHeaderVO header = CommonHeaderVO.builder()
                .dataDivision(String.valueOf(AmountType.PAYMENT))
                .managementNumber(amountEntity.getAmountId())
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
                .spareField(paymentEntity.getUserId())
                .build();

        // String data 생성 및 전송
        String stringData = getAndSendStringData(header, sender);

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
        PaymentEntity paymentEntity = getPaymentEntity(amountEntity);

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
        paymentEntity.setPaymentStatus(PaymentStatus.CANCELLATION);
        paymentEntity.setInstallmentMonths(0);
        paymentEntity.setPaymentModifiedData();

        // 카드정보 복호화 (카드번호/만료일자/cvc)
        CardInformationVO cardInformation = cardInformationConversionService.getCardInformation(paymentEntity);

        // 전달할 String data 헤더, 데이터 객체 생성
        CommonHeaderVO header = CommonHeaderVO.builder()
                .dataDivision(String.valueOf(AmountType.CANCEL))
                .managementNumber(amountEntity.getAmountId())
                .build();
        header.setTotalDataLength();
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber(cardInformation.getCardNumber())
                .installmentMonths(paymentEntity.getInstallmentMonths())
                .expirationPeriod(cardInformation.getExpirationPeriod())
                .cvc(Integer.parseInt(cardInformation.getCvc()))
                .amount(paymentEntity.getRestAmount())
                .vat(paymentEntity.getRestVat())
                .originalManagementNumber(getFirstPaymentAmountId(paymentEntity))
                .encryptedCardInformation(paymentEntity.getCardInformation())
                .spareField(paymentEntity.getUserId())
                .build();

        // String data 생성 및 전송
        String stringData = getAndSendStringData(header, sender);

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
                .cardNumber(cardInformationConversionService.maskingCardNumber(cardInformation.getCardNumber()))
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
        partialCancellationSO.partialCancellationValidationCheck();

        // 올바른 amount id인지 판단 및 예외처리
        AmountEntity amountEntity = getAmountEntityById(partialCancellationSO.getAmountId());

        // 해당 결제 정보 반환
        PaymentEntity paymentEntity = getPaymentEntity(amountEntity);

        // 취소 가능 여부 판단
        partialCancellationAvailableCheck(partialCancellationSO, paymentEntity.getRestAmount(), paymentEntity.getRestVat());

        // 부가가치세 설정
        PartialCancellationVO partialCancellationVO = new PartialCancellationVO(partialCancellationSO);

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

        // 결제상태, 할부개월수 데이터 저장
        if(paymentEntity.getRestAmount() == partialCancellationVO.getAmount()) {
            paymentEntity.setPaymentStatus(PaymentStatus.CANCELLATION);
            paymentEntity.setInstallmentMonths(0);
        } else {
            paymentEntity.setPaymentStatus(PaymentStatus.PARTIAL_CANCELLATION);
        }
        paymentEntity.setPaymentModifiedData();

        // 카드정보 복호화 (카드번호/만료일자/cvc)
        CardInformationVO cardInformation = cardInformationConversionService.getCardInformation(paymentEntity);

        // 전달할 String data 헤더, 데이터 객체 생성
        CommonHeaderVO header = CommonHeaderVO.builder()
                .dataDivision(String.valueOf(AmountType.CANCEL))
                .managementNumber(amountEntity.getAmountId())
                .build();
        header.setTotalDataLength();
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber(cardInformation.getCardNumber())
                .installmentMonths(paymentEntity.getInstallmentMonths())
                .expirationPeriod(cardInformation.getExpirationPeriod())
                .cvc(Integer.parseInt(cardInformation.getCvc()))
                .amount(paymentEntity.getRestAmount())
                .vat(paymentEntity.getRestVat())
                .originalManagementNumber(getFirstPaymentAmountId(paymentEntity))
                .encryptedCardInformation(paymentEntity.getCardInformation())
                .spareField(paymentEntity.getUserId())
                .build();

        // String data 생성 및 전송
        String stringData = getAndSendStringData(header, sender);

        return PaymentResultVO.builder()
                .amountId(cancelAmount.getAmountId())
                .stringData(stringData).build();
    }

    // String data 생성 및 전송
    private String getAndSendStringData(CommonHeaderVO header, DataSenderVO sender) {
        String stringData = header.getStringData() + sender.getStringData();

        // String data 전송
        boolean sendDateSuccess = cardApiService.sendHttpToCardApi(stringData);
        // 통신 실패 시(예외 발생) 예외 발생
        if(!sendDateSuccess) {
            throw new CustomException(ErrorCode.TRANSFER_DATA_FAIL);
        }
        return stringData;
    }

    // 올바른 amount id인지 판단 및 예외처리
    private AmountEntity getAmountEntityById(String amountId) {
        AmountEntity amountEntity = amountRepository.findById(amountId)
                .orElseThrow(() -> {
                    throw new CustomException(ErrorCode.WRONG_AMOUNT_ID);
                });
        return amountEntity;
    }

    // 해당 결제 정보 반환
    private static PaymentEntity getPaymentEntity(AmountEntity amountEntity) {
        PaymentEntity paymentEntity = amountEntity.getPaymentEntity();
        // 이미 취소된 결제 건인지 확인
        if (paymentEntity.getPaymentStatus() == PaymentStatus.CANCELLATION) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED_PAYMENT);
        }
        return paymentEntity;
    }

    // 취소 가능 여부 판단
    private static void partialCancellationAvailableCheck(PartialCancellationSO partialCancellationSO, int restAmount, int restVat) {
        // 취소 금액이 잔여금액보다 큰 경우
        if (restAmount < partialCancellationSO.getAmount()) {
            throw new CustomException(ErrorCode.EXCEED_REST_AMOUNT);
        }
        // 취소 부가가치세가 잔여 부가가치세보다 큰 경우
        if (partialCancellationSO.getVat() != null
                && restVat < partialCancellationSO.getVat()) {
            throw new CustomException(ErrorCode.EXCEED_REST_VAT);
        }
        // 잔여 부가가치세가 남는 경우
        if (restAmount == partialCancellationSO.getAmount()
                && partialCancellationSO.getVat() != null
                && restVat - partialCancellationSO.getVat() > 0) {
            throw new CustomException(ErrorCode.EXIST_REST_VAT);
        }
    }

    // 최초 결제 ID
    private static String getFirstPaymentAmountId(PaymentEntity paymentEntity) {
        // 전체 결제건에 대하여 최초 결제 id를 불러옴
        String firstPaymentAmountId = paymentEntity.getAmounts().stream()
                .filter(amount -> amount.getAmountType() == AmountType.PAYMENT)
                .findFirst()
                .orElseThrow(() -> {
                    throw new CustomException(ErrorCode.WRONG_PAYMENT);
                }).getAmountId();
        return firstPaymentAmountId;
    }

}
