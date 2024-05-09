package com.paymentPractice.payment.repository;

import com.paymentPractice.payment.entity.TransferDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferDataRepository extends JpaRepository<TransferDataEntity, Long> {
}
