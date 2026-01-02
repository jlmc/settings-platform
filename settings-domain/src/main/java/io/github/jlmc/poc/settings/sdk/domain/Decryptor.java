package io.github.jlmc.poc.settings.sdk.domain;

public interface Decryptor {
    String decrypt(String message, String privateKeyStr);
}
