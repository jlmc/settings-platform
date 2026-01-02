package io.gihub.jlmc.poc.commons.settings.redis.sdk.contracts;

public interface Decryptor {
    String decrypt(String message, String privateKeyStr);
}
