package io.github.jlmc.settings.client.ports.out;

import io.github.jlmc.settings.client.core.ConfigurationRequest;

public interface DistributedConfigProvider extends AutoCloseable {

    <T> T getOrNull(
            ConfigurationRequest request,
            Class<T> responseType
    );

    @Override
    void close() throws Exception;
}
