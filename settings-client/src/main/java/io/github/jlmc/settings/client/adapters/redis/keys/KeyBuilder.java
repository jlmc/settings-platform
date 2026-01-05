package io.github.jlmc.settings.client.adapters.redis.keys;

import io.github.jlmc.settings.client.core.ConfigurationRequest;

public interface KeyBuilder {

    String build(ConfigurationRequest request);
}
