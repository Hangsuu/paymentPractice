package com.paymentPractice.testHelper;

import com.paymentPractice.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTestRepository extends JpaRepository<PaymentEntity, String> {
    void deleteByUserId(String userId);
    List<PaymentEntity> findAllByUserId(String userId);
}