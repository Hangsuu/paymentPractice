package com.paymentPractice.payment.service.impl;

import com.paymentPractice.payment.entity.TransferDataEntity;
import com.paymentPractice.payment.repository.TransferDataRepository;
import com.paymentPractice.payment.service.CardApiService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
public class CardApiServiceImpl implements CardApiService {
    private final TransferDataRepository transferDataRepository;

    @Override
    @Transactional
    // 카드사 통신(가정) API
    public boolean sendHttpToCardApi(String data) {
        try {
            TransferDataEntity transferDataEntity = TransferDataEntity.builder()
                    .stringData(data)
                    .build();
            transferDataRepository.save(transferDataEntity);
            log.info("카드사 API 통신 요청 및 성공 : {}", data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
