package com.paymentPractice.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name="TRANSFER_DATA")
public class TransferDataEntity {
    @Id
    @GeneratedValue
    private Long id;

    // 전송 String 데이터
    @Column(length = 450)
    private String stringData;
}
