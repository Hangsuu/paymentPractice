package com.paymentPractice.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name="CARD_NUMBER")
public class CardNumberEntity {
    @Id
    @Column(length = 20)
    private String cardNumber;
    @Version
    private Long version;

    public CardNumberEntity(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}