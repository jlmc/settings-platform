package io.github.jlmc.settings.client.adapters.redis.providers;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record RedisKeyGenerator(String namespace) {

    String generateFullKey(String... segments) {
        return Stream.concat(
                        Stream.of(namespace),
                        Stream.of(segments)
                ).filter(Objects::nonNull)
                .collect(Collectors.joining(":"));
    }
}
