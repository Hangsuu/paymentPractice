package com.paymentPractice.payment.service;

public interface CardApiService {
    /**
     * 카드사 통신(가정) API
     * @param data
     * @return boolean
     */
    boolean sendHttpToCardApi(String data);
}
