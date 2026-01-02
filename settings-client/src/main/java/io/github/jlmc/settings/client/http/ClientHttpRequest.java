package io.github.jlmc.settings.client.http;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public record ClientHttpRequest(
        URI uri,
        HttpMethod method,
        Map<String, String> headers,
        Duration requestTimeout,
        Body body
) {

    public ClientHttpRequest {
        Objects.requireNonNull(uri, "uri must not be null");

        method = method != null ? method : HttpMethod.GET;
        headers = headers != null ? headers : Collections.emptyMap();
    }

    public ClientHttpRequest(URI uri) {
        this(uri, HttpMethod.GET, Collections.emptyMap(), null, null);
    }
}
