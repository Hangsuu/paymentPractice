package com.paymentPractice.payment.service;

public interface CardNumberService {
    /**
     * 카드번호가 사용중인지 확인 후 미사용일 경우 카드정보 DB 저장
     * @param cardNumber
     */
    void checkUsingCardNumber(String cardNumber);

    /**
     * 카드정보 DB 삭제
     * @param cardNumber
     */
    void deleteUsedCardNumber(String cardNumber);
}