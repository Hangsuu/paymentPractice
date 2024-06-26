package com.paymentPractice.payment.entity;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="AMOUNT")
public class AmountEntity extends BaseEntity {
    @Id
    @Column(length = 20)
    private String amountId;

    // 결제금액
    private int amount;

    // 결제금액 구분
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AmountType amountType;

    // 부가가치세
    private int vat;

    // 기본부가가치세여부 (Y : /11, N : 직접입력)
    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private YesOrNo vatDefaultYn;

    // 결제아이디(FK)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PAYMENT_ID")
    private PaymentEntity paymentEntity;

    // ID 및 BaseEntity 정보 입력
    public void setAmountDataBeforeInsert() {
        createAmountId();
        // Base Entity 데이터 입력
        setDefaultDataBeforeInsert(this.paymentEntity.getUserId());
    }
    private void createAmountId() {
        StringBuilder stringBuilder = new StringBuilder();
        // amount 테이블 식별 코드 입력 (2자리)
        stringBuilder.append(TableCode.AM);
        // 현재 시간을 yyMMddHHmmss 형태로 입력 (15자리)
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
        stringBuilder.append(currentTime.format(formatter));
        // UUID 랜덤 코드 입력 (3자리)
        stringBuilder.append(UUID.randomUUID().toString().substring(0, 3));

        this.amountId = stringBuilder.toString();
    }

    public PaymentEntity getPaymentEntityWithCancellationCheck() {
        // 이미 취소된 결제 건인지 확인
        if (this.paymentEntity.getPaymentStatus() == PaymentStatus.CANCELLATION) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED_PAYMENT);
        }
        return this.paymentEntity;
    }

}
