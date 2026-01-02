package io.github.jlmc.settings.client.redis;

import io.github.jlmc.settings.client.ConfigurationRequest;

public interface DistributedConfigProvider extends AutoCloseable {

    <T> T getOrNull(
            ConfigurationRequest request,
            Class<T> responseType
    );

    @Override
    void close() throws Exception;
}
