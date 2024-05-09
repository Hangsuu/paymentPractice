package com.paymentPractice.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="PAYMENT")
public class PaymentEntity extends BaseEntity {
    @Id
    @Column(length = 20)
    private String paymentId;

    // 유저 아이디
    @Column(length = 20)
    private String userId;

    // 카드 정보(카드번호/유효기간/CVC 암호화)
    private String cardInformation;

    // 결제 상태
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private PaymentStatus paymentStatus;

    // 할부 개월수
    private int installmentMonths;

    @OneToMany(mappedBy = "paymentEntity", cascade = CascadeType.ALL)
    private List<AmountEntity> amounts = new ArrayList<>();

    @Version
    private Long version;

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    public void setInstallmentMonths(int installmentMonths) {
        this.installmentMonths = installmentMonths;
    }

    // ID 및 BaseEntity 정보 입력
    public void setPaymentInsertData() {
        StringBuilder stringBuilder = new StringBuilder();
        // payment 테이블 식별 코드 입력 (2자리)
        stringBuilder.append(TableCode.PA);
        // 현재 시간을 yyMMddHHmmss 형태로 입력 (15자리)
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
        stringBuilder.append(currentTime.format(formatter));
        // UUID 랜덤 코드 입력 (3자리)
        stringBuilder.append(UUID.randomUUID().toString().substring(0, 3));

        this.paymentId = stringBuilder.toString();
        setBaseInsertData(this.userId);
    }

    public void addAmount(AmountEntity amountEntity) {
        amountEntity.setPaymentEntity(this);
        amounts.add(amountEntity);
    }

    public void setPaymentModifiedData() {
        setBaseModifiedData(this.userId);
    }
}
