package com.paymentPractice.common.service;

public interface TwoWayEncryptionService {
    String encrypt(String value);
    String decrypt(String value);
}
