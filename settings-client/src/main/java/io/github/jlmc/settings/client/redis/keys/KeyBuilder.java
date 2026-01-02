package io.github.jlmc.settings.client.redis.keys;

import io.github.jlmc.settings.client.ConfigurationRequest;

public interface KeyBuilder {

    String build(ConfigurationRequest request);
}
