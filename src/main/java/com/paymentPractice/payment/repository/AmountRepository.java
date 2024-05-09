package com.paymentPractice.payment.repository;

import com.paymentPractice.payment.entity.AmountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmountRepository extends JpaRepository<AmountEntity, String> {
}
