package com.paymentPractice.payment.service.impl;

import com.paymentPractice.common.exception.CustomException;
import com.paymentPractice.common.model.ErrorCode;
import com.paymentPractice.payment.entity.CardNumberEntity;
import com.paymentPractice.payment.repository.CardNumberRepository;
import com.paymentPractice.payment.service.CardNumberService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CardNumberServiceImpl implements CardNumberService {
    private final CardNumberRepository cardNumberRepository;

    @Override
    @Transactional
    // 카드번호가 사용중인지 확인 후 미사용일 경우 카드정보 DB 저장
    public void checkUsingCardNumber(String cardNumber) {
        Optional<CardNumberEntity> usingCardNumber = cardNumberRepository.findById(cardNumber);
        if(usingCardNumber.isPresent()) {
            throw new CustomException(ErrorCode.USING_CARD_NUMBER);
        } else {
            cardNumberRepository.save(new CardNumberEntity(cardNumber));
        }
    }

    @Override
    @Transactional
    // 카드정보 DB 삭제
    public void deleteUsedCardNumber(String cardNumber) {
        cardNumberRepository.deleteById(cardNumber);
    }
}