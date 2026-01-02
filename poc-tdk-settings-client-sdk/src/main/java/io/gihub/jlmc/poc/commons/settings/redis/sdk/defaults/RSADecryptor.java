package io.gihub.jlmc.poc.commons.settings.redis.sdk.defaults;

import io.gihub.jlmc.poc.commons.settings.redis.sdk.contracts.Decryptor;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;


public class RSADecryptor implements Decryptor {

    private final ConcurrentHashMap<String, PrivateKey> KEY_CONCURRENT_HASH_MAP = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ThreadLocal<Cipher>> CIPHER_CACHE = new ConcurrentHashMap<>();

    /**
     * Decrypts a message using the given private key.
     *
     * @param message       Base64-encoded encrypted message
     * @param privateKeyStr Base64-encoded PKCS#8 private key
     * @return decrypted plaintext
     */
    @Override
    public String decrypt(String message, String privateKeyStr) {
        try {
            PrivateKey privateKey = KEY_CONCURRENT_HASH_MAP.computeIfAbsent(privateKeyStr, this::generatePrivateKey);

            ThreadLocal<Cipher> cipherThreadLocal = CIPHER_CACHE.computeIfAbsent(privateKeyStr, k -> ThreadLocal.withInitial(() -> {
                try {
                    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    return cipher;
                } catch (Exception e) {
                    throw new RuntimeException("Failed to initialize RSA Cipher", e);
                }
            }));

            Cipher cipher = cipherThreadLocal.get();
            byte[] messageBytes = Base64.getDecoder().decode(message);
            byte[] decryptedBytes = cipher.doFinal(messageBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt message", e);
        }
    }

    private PrivateKey generatePrivateKey(String keyStr) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(keyStr);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PrivateKey from string", e);
        }
    }
}
