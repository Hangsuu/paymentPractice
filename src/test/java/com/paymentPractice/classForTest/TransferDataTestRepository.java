package com.paymentPractice.classForTest;

import com.paymentPractice.payment.entity.TransferDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransferDataTestRepository extends JpaRepository<TransferDataEntity, Long> {
    @Modifying //영속성 컨텍스트를 거치지 않고, 바로 데이터베이스에 쿼리를 반영하도록 해주는 애너테이션
    @Query("delete from TransferDataEntity t where t.stringData like %:uuidUserId%")
    void deleteTestData(@Param("uuidUserId")String uuidUserId);
}
