package io.github.jlmc.poc.commons.settings.redis.keys;

import io.github.jlmc.poc.commons.settings.ConfigurationRequest;

public interface KeyBuilder {

    String build(ConfigurationRequest request);
}
