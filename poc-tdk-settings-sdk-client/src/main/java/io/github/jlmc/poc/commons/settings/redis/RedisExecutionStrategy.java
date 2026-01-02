package io.github.jlmc.poc.commons.settings.redis;

import io.github.jlmc.poc.commons.settings.ConfigurationRequest;

public interface RedisExecutionStrategy {

    <T> T getOrNull(
            ConfigurationRequest request,
            Class<T> responseType
    );


}
