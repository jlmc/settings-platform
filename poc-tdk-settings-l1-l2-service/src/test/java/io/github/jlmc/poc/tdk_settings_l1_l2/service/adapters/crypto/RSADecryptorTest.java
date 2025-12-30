package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.crypto;

import org.junit.jupiter.api.Test;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RSADecryptorTest {

    private final RSADecryptor victim = new RSADecryptor();
    private final KeyPair keyPair = generateRSAKeyPair();

    @Test
    public void decrypt_shouldCorrectlyDecryptValidEncryptedMessage() {
        String original = "Hello, RSADecryptor!";
        String encrypted = encryptWithPublicKey(original, keyPair.getPublic());
        String decrypted = victim.decrypt(encrypted, encodePrivateKey(keyPair.getPrivate().getEncoded()));
        assertEquals(original, decrypted);
    }

    @Test
    public void decrypt_shouldThrowExceptionWithInvalidPrivateKey() {
        String original = "Test message";
        String encrypted = encryptWithPublicKey(original, keyPair.getPublic());
        String invalidKey = Base64.getEncoder().encodeToString("invalid-key".getBytes());

        assertThrows(RuntimeException.class, () -> {
            // RSADecryptor wraps exceptions into RuntimeException
            victim.decrypt(encrypted, invalidKey);
        });
    }

    @Test
    public void decrypt_shouldThrowExceptionWithCorruptedEncryptedMessage() {
        String original = "Test message";
        String encrypted = encryptWithPublicKey(original, keyPair.getPublic());
        String corrupted = encrypted.substring(0, encrypted.length() / 2); // truncate

        assertThrows(RuntimeException.class, () -> victim.decrypt(corrupted, encodePrivateKey(keyPair.getPrivate().getEncoded())));
    }

    // --- Helper methods ---

    private KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }

    private String encryptWithPublicKey(String message, PublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt message with public key", e);
        }
    }

    private String encodePrivateKey(byte[] keyBytes) {
        return Base64.getEncoder().encodeToString(keyBytes);
    }
}
