package io.github.jlmc.settings.service.domain.inputs;

import io.github.jlmc.settings.service.domain.entities.ConfigurationType;

public record ResolveConfigurationInput(
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
