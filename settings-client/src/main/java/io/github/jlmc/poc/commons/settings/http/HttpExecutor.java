package io.github.jlmc.poc.commons.settings.http;

import java.net.http.HttpClient;

public class HttpExecutor {

    private final HttpClient httpClient;

    public HttpExecutor(String baseUrl) {
        httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}
