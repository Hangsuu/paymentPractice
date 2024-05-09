package com.paymentPractice.payment.repository;

import com.paymentPractice.payment.entity.CardNumberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CardNumberRepository extends JpaRepository<CardNumberEntity, String> {
}