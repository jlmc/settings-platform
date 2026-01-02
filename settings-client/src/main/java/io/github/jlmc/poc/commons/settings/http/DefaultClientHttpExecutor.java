package io.github.jlmc.poc.commons.settings.http;

import io.github.jlmc.poc.commons.settings.exceptions.SettingsClientException;
import io.github.jlmc.poc.commons.settings.exceptions.SettingsClientHttpException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultClientHttpExecutor implements ClientHttpExecutor {

    private static final int MAX_BODY_PREVIEW_LENGTH = 300;

    private final Duration defaultRequestTimeout;
    private final String userAgent;
    private final HttpClient httpClient;

    public DefaultClientHttpExecutor() {
        this(
                HttpConstants.DEFAULT_CONNECTION_TIMEOUT,
                null,
                null,
                null
        );
    }

    public DefaultClientHttpExecutor(
            Duration connectionTimeout,
            Duration defaultRequestTimeout,
            String userAgent,
            HttpClient httpClient
    ) {
        this.defaultRequestTimeout = defaultRequestTimeout;
        this.userAgent = userAgent;
        this.httpClient = httpClient != null
                ? httpClient
                : HttpClient.newBuilder()
                .connectTimeout(connectionTimeout)
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @Override
    public ClientHttpResponse<String> send(ClientHttpRequest request) {
        try {
            HttpRequest.Builder builder = httpRequestBuilder(request);

            Body body = request.body();
            if (body == null) {
                builder.method(
                        request.method().name(),
                        HttpRequest.BodyPublishers.noBody()
                );
            } else if (body instanceof Body.Form form) {
                String encoded = form.data().entrySet().stream()
                        .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                        .collect(Collectors.joining("&"));

                builder.header(
                        HttpConstants.HEADER_CONTENT_TYPE,
                        HttpConstants.CONTENT_TYPE_FORM
                ).method(
                        request.method().name(),
                        HttpRequest.BodyPublishers.ofString(encoded)
                );
            } else if (body instanceof Body.StringBody stringBody) {
                builder.header(
                        HttpConstants.HEADER_CONTENT_TYPE,
                        stringBody.contentType()
                ).method(
                        request.method().name(),
                        HttpRequest.BodyPublishers.ofString(stringBody.value())
                );
            }

            HttpResponse<String> response = httpClient.send(
                    builder.build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            return checkAndExtract(response);

        } catch (IOException e) {
            throw new SettingsClientException(
                    "I/O error while calling " + request.uri(),
                    e
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SettingsClientException(
                    "HTTP request interrupted at " + request.uri(),
                    e
            );
        }
    }

    private HttpRequest.Builder httpRequestBuilder(ClientHttpRequest request) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(request.uri());

        if (userAgent != null) {
            builder.header(HttpConstants.HEADER_USER_AGENT, userAgent);
        }

        for (Map.Entry<String, String> entry : request.headers().entrySet()) {
            builder.header(entry.getKey(), entry.getValue());
        }

        Duration timeout = request.requestTimeout() != null
                ? request.requestTimeout()
                : defaultRequestTimeout;

        if (timeout != null) {
            builder.timeout(timeout);
        }

        return builder;
    }

    private ClientHttpResponse<String> checkAndExtract(HttpResponse<String> response) {
        int status = response.statusCode();

        if (status < 200 || status > 299) {
            HttpStatusCode httpStatusCode = HttpStatusCode.fromCode(status);
            String reason = httpStatusCode != null
                    ? httpStatusCode.getReason()
                    : "Unknown reason";

            String bodyPreview = response.body() != null
                    ? response.body().substring(
                    0,
                    Math.min(MAX_BODY_PREVIEW_LENGTH, response.body().length())
            )
                    : "<no body>";

            String message = String.format(
                    "HTTP %d (%s) from %s. Response body (truncated): %s",
                    status,
                    reason,
                    response.uri(),
                    bodyPreview
            );

            throw new SettingsClientHttpException(status, message);
        }

        Map<String, String> headers = response.headers().map().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.join(",", e.getValue())
                ));

        return new ClientHttpResponse<>(
                status,
                response.body(),
                headers,
                null
        );
    }

    public static URI appendPath(URI uri, String... segments) throws URISyntaxException {
        StringBuilder newPath = new StringBuilder();

        String currentPath = uri.getPath() != null
                ? uri.getPath().replaceAll("/+$", "")
                : "";

        newPath.append(currentPath);

        for (String segment : segments) {
            String clean = segment.replaceAll("^/|/$", "");
            if (!clean.isEmpty()) {
                newPath.append('/').append(clean);
            }
        }

        String finalPath = newPath.toString().startsWith("/")
                ? newPath.toString()
                : "/" + newPath;

        return new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                finalPath,
                uri.getQuery(),
                uri.getFragment()
        );
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        this.httpClient.shutdown();
    }
}
