package io.github.jlmc.settings.client.adapters.redis.keys;

import io.github.jlmc.settings.client.core.ConfigurationRequest;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StandardKeyBuilder implements KeyBuilder {

    private final String namespace;
    private final Function<ConfigurationRequest, String> accountIdProvider;

    public StandardKeyBuilder(String namespace,
                              Function<ConfigurationRequest, String> accountIdProvider) {
        this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null");
        this.accountIdProvider = Objects.requireNonNull(accountIdProvider, "accountIdProvider cannot be null");
    }


    @Override
    public String build(ConfigurationRequest request) {
        String service = request.service();
        String accountId = accountIdProvider.apply(request);

        // settings:1:my-service:ACCOUNT
        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("service cannot be blank");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId cannot be blank");
        }

        // Build key components
        return Stream.of(
                        namespace,
                        accountId,
                        service,
                        request.objectType().name(),
                        request.objectId() != null && !request.objectId().isBlank() ? request.objectId() : null
                )
                .filter(Objects::nonNull)
                .collect(Collectors.joining(":"));
    }
}
