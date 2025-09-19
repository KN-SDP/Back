package com.knusdp.SmartLedger.util;

import com.knusdp.SmartLedger.config.CryptoProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class CryptoUtil {

    private final CryptoProperties cryptoProperties;

    public CryptoUtil(CryptoProperties cryptoProperties) {
        this.cryptoProperties = cryptoProperties;
    }

    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(cryptoProperties.getInitVector().getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(cryptoProperties.getSecretKey().getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException("데이터 암호화 중 오류 발생", ex);
        }
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(cryptoProperties.getInitVector().getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(cryptoProperties.getSecretKey().getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("데이터 복호화 중 오류 발생", ex);
        }
    }
}
