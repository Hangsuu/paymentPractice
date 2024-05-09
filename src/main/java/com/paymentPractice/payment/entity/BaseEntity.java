package com.paymentPractice.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {
    // 최종 수정자 아이디
    @Column(length = 20)
    private String lastModifiedId;

    // 최종 수정 일시
    private LocalDateTime lastModifiedDate;

    // 생성자 아이디
    @Column(length = 20)
    private String creationId;

    // 생성 일시
    private LocalDateTime creationDate;

    // 삭제 여부
    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private YesOrNo deleteYn;

    protected void setBaseInsertData(String userId) {
        this.lastModifiedId = userId;
        this.lastModifiedDate = LocalDateTime.now();
        this.creationId = userId;
        this.creationDate = LocalDateTime.now();
        this.deleteYn = YesOrNo.N;
    }
    protected void setBaseModifiedData(String userId) {
        this.lastModifiedId = userId;
        this.lastModifiedDate = LocalDateTime.now();
    }
}
