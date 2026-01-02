package io.gihub.jlmc.poc.commons.settings.http;

import java.time.Duration;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toUnmodifiableSet;

public final class HttpConstants {

    private HttpConstants() {
        // utility class
    }

    /** Common HTTP header names. */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_RSA_PRIVATE_KEY = "X-Private-Key";
    public static final String HEADER_INTERACTION_ID = "X-Interaction-Id";

    /** Common content types. */
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_FORM =
            "application/x-www-form-urlencoded; charset=UTF-8";

    public static final int HTTP_STATUS_NOT_FOUND = 404;

    /** 2xx HTTP status codes */
    public static final Set<Integer> HTTP_STATUS_2XX =
            IntStream.rangeClosed(200, 299)
                    .boxed()
                    .collect(toUnmodifiableSet());

    /** Default timeouts for HTTP requests. */
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(5);
}
