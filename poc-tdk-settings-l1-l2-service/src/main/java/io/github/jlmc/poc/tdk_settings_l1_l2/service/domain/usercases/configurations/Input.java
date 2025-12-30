package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;

public record Input(
        String accountId,
        String serviceName,
        ConfigurationType configurationType,
        String privateKey
) {

    @Override
    public String toString() {
        return String.format(
                "Input{accountId='%s', serviceName='%s', configurationType=%s, privateKey=%s}",
                accountId,
                serviceName,
                configurationType,
                privateKey != null ? "[PROTECTED]" : null
        );
    }

    public boolean hasPrivateKey() {
        return this.privateKey != null && !this.privateKey.isBlank();
    }
}
