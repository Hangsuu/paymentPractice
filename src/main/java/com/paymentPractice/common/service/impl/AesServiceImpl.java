package com.paymentPractice.common.service.impl;

import com.paymentPractice.common.service.TwoWayEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
@Slf4j
public class AesServiceImpl implements TwoWayEncryptionService {
    @Value("${aes.key}")
    private String key;
    @Value("${aes.alg}")
    private String alg;
    public static byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);

            byte[] encrypted = cipher.doFinal(value.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (NullPointerException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);

            // Base64로 디코딩 후 AES decrypt
            byte[] encryptedBytes = Base64.getDecoder().decode(value);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            // String으로 변환하여 반환
            return new String(decryptedBytes);
        } catch (NullPointerException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
