package com.paymentPractice.testHelper;

import com.paymentPractice.payment.entity.TransferDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferDataTestRepository extends JpaRepository<TransferDataEntity, Long> {
    @Modifying
    @Query("delete from TransferDataEntity t where t.stringData like %:uuidUserId%")
    void deleteTestData(@Param("uuidUserId")String uuidUserId);
}