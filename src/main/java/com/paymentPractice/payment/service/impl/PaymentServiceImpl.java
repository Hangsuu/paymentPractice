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
    private final ValidationCheckService validationCheckService;
    private final CardInformationConversionService cardInformationConversionService;

    @Override
    @Transactional
    @MethodLog(description = "결제 메서드")
    public PaymentResultVO payment(PaymentSO paymentSO) {
        // 결제 유효성 체크
        validationCheckService.paymentValidationCheck(paymentSO);

        // 카드정보 암호화
        String encryptedCardInformation = cardInformationConversionService.getEncryptedCardInformation(paymentSO);

        // 부가가치세 설정
        CalculatedVat calculatedVat = getCalculatedVat(paymentSO.getVat(), paymentSO.getAmount());

        // 데이터 저장
        PaymentEntity paymentEntity = PaymentEntity.builder()
                .userId(paymentSO.getUserId() == null ? "tempUserId" : paymentSO.getUserId())
                .paymentStatus(PaymentStatus.SUCCESS) // 결제 상태
                .cardInformation(encryptedCardInformation) // 카드 정보(암호화)
                .installmentMonths(paymentSO.getInstallmentMonths()) // 할부 개월수
                .build();
        paymentEntity.setPaymentInsertData();
        paymentRepository.save(paymentEntity);
        AmountEntity amountEntity = AmountEntity.builder()
                .amount(paymentSO.getAmount()) // 결제 금액
                .paymentEntity(paymentEntity)
                .amountType(AmountType.PAYMENT) // 결제금액 구분
                .vat(calculatedVat.getVat()) // 부가가치세
                .vatDefaultYn(calculatedVat.getVatDefaultYn()) // 기본부가가치세여부
                .build();
        amountEntity.setAmountInsertData();
        amountRepository.save(amountEntity);

        // 전달할 String data 헤더, 데이터 객체 생성
        CommonHeaderVO header = CommonHeaderVO.builder()
                .dataLength(getHeaderAndDataLength())
                .dataDivision(String.valueOf(AmountType.PAYMENT)) // 데이터 구분
                .managementNumber(amountEntity.getAmountId()) // 관리번호
                .build();
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber(paymentSO.getCardNumber())
                .installmentMonths(paymentSO.getInstallmentMonths()) // 할부 개월수
                .expirationPeriod(paymentSO.getExpirationPeriod()) // 카드 유효기간
                .cvc(Integer.parseInt(paymentSO.getCvc()))
                .amount(paymentSO.getAmount()) // 결제금액
                .vat(calculatedVat.getVat()) // 부가가치세
                .originalManagementNumber("") // 원거래 관리번호
                .encryptedCardInformation(encryptedCardInformation) // 암호화된 카드정보
                .spareField(paymentEntity.getUserId()) // 예비필드(사용자 id)
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

        // 남은 결제금액과 부가가치세 계산
        RestAmountAndVat restAmountAndVat = getRestAmountAndVat(paymentEntity);

        // 금액 데이터 저장
        AmountEntity cancelAmount = AmountEntity.builder()
                .amount(restAmountAndVat.getAmount()) // 결제금액
                .paymentEntity(paymentEntity)
                .amountType(AmountType.CANCEL) // 결제금액 구분
                .vat(restAmountAndVat.getVat()) // 부가가치세
                .vatDefaultYn(YesOrNo.N) // 기본부가가치세여부
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
                .dataLength(getHeaderAndDataLength())
                .dataDivision(String.valueOf(AmountType.CANCEL)) // 데이터 구분
                .managementNumber(amountEntity.getAmountId()) // 관리번호
                .build();
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber(cardInformation.getCardNumber())
                .installmentMonths(paymentEntity.getInstallmentMonths()) // 할부 개월수
                .expirationPeriod(cardInformation.getExpirationPeriod()) // 카드 유효 기간
                .cvc(Integer.parseInt(cardInformation.getCvc()))
                .amount(restAmountAndVat.getAmount()) // 결제금액
                .vat(restAmountAndVat.getVat()) // 부가가치세
                .originalManagementNumber(getFirstPaymentAmountId(paymentEntity)) // 원거래 관리번호(최초결제 ID)
                .encryptedCardInformation(paymentEntity.getCardInformation()) // 암호화된 카드정보
                .spareField(paymentEntity.getUserId()) // 예비필드(사용자 id)
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
        validationCheckService.partialCancellationValidationCheck(partialCancellationSO);

        // 올바른 amount id인지 판단 및 예외처리
        AmountEntity amountEntity = getAmountEntityById(partialCancellationSO.getAmountId());

        // 해당 결제 정보 반환
        PaymentEntity paymentEntity = getPaymentEntity(amountEntity);

        // 남은 결제금액과 부가가치세 계산
        RestAmountAndVat restAmountAndVat = getRestAmountAndVat(paymentEntity);

        // 취소 가능 여부 판단
        partialCancellationAvailableCheck(partialCancellationSO, restAmountAndVat.getAmount(), restAmountAndVat.getVat());

        // 부가가치세 설정
        CalculatedVat calculatedVat = getCalculatedVat(partialCancellationSO.getVat(), partialCancellationSO.getAmount());

        // 금액 데이터 저장
        AmountEntity cancelAmount = AmountEntity.builder()
                .amount(partialCancellationSO.getAmount()) // 결제금액
                .paymentEntity(paymentEntity)
                .amountType(AmountType.CANCEL) // 결제금액 구분(PAYMENT, CANCEL)
                .vat(calculatedVat.getVat()) // 부가가치세
                .vatDefaultYn(calculatedVat.getVatDefaultYn()) // 기본부가가치세여부
                .build();
        cancelAmount.setAmountInsertData();
        amountRepository.save(cancelAmount);

        // 결제상태, 할부개월수 데이터 저장
        if(restAmountAndVat.getAmount() == partialCancellationSO.getAmount()) {
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
                .dataLength(getHeaderAndDataLength())
                .dataDivision(String.valueOf(AmountType.CANCEL)) // 데이터 구분
                .managementNumber(amountEntity.getAmountId()) // 관리번호
                .build();
        DataSenderVO sender = DataSenderVO.builder()
                .cardNumber(cardInformation.getCardNumber()) // 카드번호
                .installmentMonths(paymentEntity.getInstallmentMonths()) // 할부 개월수
                .expirationPeriod(cardInformation.getExpirationPeriod()) // 카드 유효 기간
                .cvc(Integer.parseInt(cardInformation.getCvc())) // cvc
                .amount(restAmountAndVat.getAmount()) // 결제금액
                .vat(restAmountAndVat.getVat()) // 부가가치세
                .originalManagementNumber(getFirstPaymentAmountId(paymentEntity)) // 원거래 관리번호(최초결제 id)
                .encryptedCardInformation(paymentEntity.getCardInformation()) // 암호화된 카드정보
                .spareField(paymentEntity.getUserId()) // 예비필드(사용자 id)
                .build();

        // String data 생성 및 전송
        String stringData = getAndSendStringData(header, sender);

        return PaymentResultVO.builder()
                .amountId(cancelAmount.getAmountId())
                .stringData(stringData).build();
    }

    // 부가가치세 설정
    private static CalculatedVat getCalculatedVat(Integer vatData, int amount) {
        int vat = 0;
        YesOrNo vatDefaultYn = null;
        // 부가가치세를 null로 받아 /11 을 적용받는 경우 
        if (vatData == null) {
            vat = (int) Math.round((double) amount / 11.0);
            vatDefaultYn = YesOrNo.Y;
        } else { // 부가가치세를 설정한 경우
            vat = vatData;
            vatDefaultYn = YesOrNo.N;
        }
        return CalculatedVat.builder()
                .vat(vat)
                .vatDefaultYn(vatDefaultYn)
                .build();
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
                    // (400) 잘못된 id입니다.
                    throw new CustomException(ErrorCode.WRONG_AMOUNT_ID);
                });
        return amountEntity;
    }

    // 해당 결제 정보 반환
    private static PaymentEntity getPaymentEntity(AmountEntity amountEntity) {
        PaymentEntity paymentEntity = amountEntity.getPaymentEntity();
        // 이미 취소된 결제 건인지 확인
        if (paymentEntity.getPaymentStatus() == PaymentStatus.CANCELLATION) {
            // (400) 이미 취소처리된 결제입니다.
            throw new CustomException(ErrorCode.ALREADY_CANCELLED_PAYMENT);
        }
        return paymentEntity;
    }

    // 남은 결제금액과 부가가치세 계산
    private RestAmountAndVat getRestAmountAndVat(PaymentEntity paymentEntity) {
        List<AmountEntity> amounts = paymentEntity.getAmounts();
        // 금액 및 부가가치세를 PAYMENT면 + CANCEL이면 -해서 합함
        int restAmount = amounts.stream()
                .mapToInt(amount -> amount.getAmountType() == AmountType.PAYMENT ? amount.getAmount() : -amount.getAmount())
                .sum();
        int restVat = amounts.stream()
                .mapToInt(amount -> amount.getAmountType() == AmountType.PAYMENT ? amount.getVat() : -amount.getVat())
                .sum();
        return RestAmountAndVat.builder()
                .amount(restAmount)
                .vat(restVat).build();
    }

    // 취소 가능 여부 판단
    private static void partialCancellationAvailableCheck(PartialCancellationSO partialCancellationSO, int restAmount, int restVat) {
        // 취소 금액이 잔여금액보다 큰 경우
        if (restAmount < partialCancellationSO.getAmount()) {
            // (400) 취소 금액이 잔여 금액을 초과했습니다.
            throw new CustomException(ErrorCode.EXCEED_REST_AMOUNT);
        }
        // 취소 부가가치세가 잔여 부가가치세보다 큰 경우
        if (partialCancellationSO.getVat() != null
                && restVat < partialCancellationSO.getVat()) {
            // (400) 취소 부가가치세가 잔여 부가가치세를 초과했습니다.
            throw new CustomException(ErrorCode.EXCEED_REST_VAT);
        }
        // 잔여 부가가치세가 남는 경우
        if (restAmount == partialCancellationSO.getAmount()
                && partialCancellationSO.getVat() != null
                && restVat - partialCancellationSO.getVat() > 0) {
            // (400) 잔여 부가가치세가 존재합니다.
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

    // StrengthLength 어노테이션의 length값 총합 반환
    private int getHeaderAndDataLength() {
        int totalLength = 0;
        for (Field field : DataSenderVO.class.getDeclaredFields()) {
            // 필드에 적용된 어노테이션 정보 가져와서 반환
            StringLength annotation = field.getAnnotation(StringLength.class);
            if (annotation != null) {
                totalLength += annotation.length();
            }
        }
        for (Field field : CommonHeaderVO.class.getDeclaredFields()) {
            // 필드에 적용된 어노테이션 정보 가져와서 반환
            StringLength annotation = field.getAnnotation(StringLength.class);
            // dataLength 길이는 계산에서 제외
            if (annotation != null && !field.getName().equals("dataLength")) {
                totalLength += annotation.length();
            }
        }
        return totalLength;
    }

    @Getter
    @Setter
    @Builder
    private static class RestAmountAndVat {
        private int amount;
        private int vat;
    }

    @Getter
    @Setter
    @Builder
    private static class CalculatedVat {
        private int vat;
        YesOrNo vatDefaultYn;
    }
}
