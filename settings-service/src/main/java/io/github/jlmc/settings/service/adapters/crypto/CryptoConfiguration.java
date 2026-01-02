package io.github.jlmc.settings.service.adapters.crypto;

import io.github.jlmc.settings.service.domain.ports.Decryptor;
import io.github.jlmc.settings.service.domain.ports.SettingsAccountDecrypter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CryptoConfiguration {

    @Bean
    Decryptor Decryptor() {
        return new RSADecryptor();
    }

    @Bean
    SettingsAccountDecrypter settingsAccountDecrypter(Decryptor decryptor) {
        return new SettingsAccountJsonDecrypter(decryptor);
    }
}
