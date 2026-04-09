package jnu.econovation.ecoknockbecentral.common.security.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import jnu.econovation.ecoknockbecentral.common.exception.server.InternalServerException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AES256Util {
    public static final String ALGORITHM = "AES";
    public static final String TRANSFORMATION = ALGORITHM + "/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BIT = 128;

    @Getter
    private final String key;

    private final SecretKeySpec secretKey;

    public AES256Util(@Value("${security.aes256.key}") String key) {
        this.key = key;

        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length != 32) {
            throw new InternalServerException(
                    new IllegalArgumentException("AES-256 키는 32 bytes 길이여야 합니다.")
            );
        }

        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public String encrypt(String plainText) {
        try {
            byte[] ivBytes = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(ivBytes);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, ivBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[ivBytes.length + encrypted.length];
            System.arraycopy(ivBytes, 0, combined, 0, ivBytes.length);
            System.arraycopy(encrypted, 0, combined, ivBytes.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new InternalServerException(e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            if (decoded.length < IV_LENGTH) {
                throw new InternalServerException(
                        new IllegalArgumentException("잘못된 암호문 형식")
                );
            }

            byte[] ivBytes = new byte[IV_LENGTH];
            byte[] contentBytes = new byte[decoded.length - IV_LENGTH];

            System.arraycopy(decoded, 0, ivBytes, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, contentBytes, 0, decoded.length - IV_LENGTH);

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(TAG_LENGTH_BIT, ivBytes);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

            byte[] decrypted = cipher.doFinal(contentBytes);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new InternalServerException(e);
        }
    }
}