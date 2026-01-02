package io.github.jlmc.settings.webflux.example.domain.ports;


import io.github.jlmc.settings.client.ConfigurationRequest;
import reactor.core.publisher.Mono;

public interface IndustriesSettingsProviderPort {

     <T> Mono<T> getSettings(ConfigurationRequest configurationRequest, Class<T> responseType);
}
