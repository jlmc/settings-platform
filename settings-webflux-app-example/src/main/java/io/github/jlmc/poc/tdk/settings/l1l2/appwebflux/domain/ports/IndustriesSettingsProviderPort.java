package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.ports;


import io.github.jlmc.settings.client.ConfigurationRequest;
import reactor.core.publisher.Mono;

public interface IndustriesSettingsProviderPort {

     <T> Mono<T> getSettings(ConfigurationRequest configurationRequest, Class<T> responseType);
}
