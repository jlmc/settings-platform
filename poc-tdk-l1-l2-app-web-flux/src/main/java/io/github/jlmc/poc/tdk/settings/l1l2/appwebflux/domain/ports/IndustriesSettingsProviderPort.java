package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.ports;


import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import reactor.core.publisher.Mono;

public interface IndustriesSettingsProviderPort {

     <T> Mono<T> getSettings(ConfigurationRequest configurationRequest, Class<T> responseType);
}
