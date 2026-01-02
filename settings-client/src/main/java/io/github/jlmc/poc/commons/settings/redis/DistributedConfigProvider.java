package io.github.jlmc.poc.commons.settings.redis;

import io.github.jlmc.poc.commons.settings.ConfigurationRequest;

public interface DistributedConfigProvider extends AutoCloseable {

    <T> T getOrNull(
            ConfigurationRequest request,
            Class<T> responseType
    );

    @Override
    void close() throws Exception;
}
