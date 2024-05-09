package com.paymentPractice.common;

import com.paymentPractice.common.service.TwoWayEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
public class TwoWayEncryptionServiceTest {
    @Autowired
    TwoWayEncryptionService twoWayEncryptionService;
    @Test
    public void encryptAndDecrypt() {
        String s1 = "testWord1";
        String s2 = "testWord2";

        String encryptedS1 = twoWayEncryptionService.encrypt(s1);
        String encryptedS2 = twoWayEncryptionService.encrypt(s2);
        log.info("encrypted S1  = {}", encryptedS1);
        log.info("encrypted S2  = {}", encryptedS2);

        assertThat(encryptedS1).isNotEqualTo(encryptedS2);

        String decryptedS1 = twoWayEncryptionService.decrypt(encryptedS1);
        String decryptedS2 = twoWayEncryptionService.decrypt(encryptedS2);

        assertThat(s1).isEqualTo(decryptedS1);
        assertThat(s1).isNotEqualTo(decryptedS2);
    }
}
