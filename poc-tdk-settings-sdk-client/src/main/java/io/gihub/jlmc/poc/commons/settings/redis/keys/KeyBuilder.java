package io.gihub.jlmc.poc.commons.settings.redis.keys;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;

public interface KeyBuilder {

    String build(ConfigurationRequest request);
}
