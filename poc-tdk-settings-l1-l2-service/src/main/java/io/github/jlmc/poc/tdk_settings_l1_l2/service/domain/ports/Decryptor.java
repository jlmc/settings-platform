package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports;

public interface Decryptor {
    String decrypt(String message, String privateKeyStr);
}
