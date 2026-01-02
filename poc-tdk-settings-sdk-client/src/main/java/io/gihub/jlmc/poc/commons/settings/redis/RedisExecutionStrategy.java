package io.gihub.jlmc.poc.commons.settings.redis;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;

public interface RedisExecutionStrategy {

    <T> T getOrNull(
            ConfigurationRequest request,
            Class<T> responseType
    );


}
