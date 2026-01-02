package io.github.jlmc.settings.domain.ports;

public interface Decryptor {
    String decrypt(String message, String privateKeyStr);
}
