package io.github.jlmc.settings.service.domain.ports;

public interface Decryptor {
    String decrypt(String message, String privateKeyStr);
}
