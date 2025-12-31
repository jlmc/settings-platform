package io.gihub.jlmc.poc.commons.settings.http;

import java.util.Collections;
import java.util.Map;

import static io.gihub.jlmc.poc.commons.settings.http.HttpConstants.HTTP_STATUS_2XX;

public record ClientHttpResponse<T>(
        int status,
        T body,
        Map<String, String> headers,
        Throwable error
) {

    public ClientHttpResponse {
        headers = headers != null ? headers : Collections.emptyMap();
    }

    public static <T> ClientHttpResponse<T> error(Throwable t) {
        return new ClientHttpResponse<>(-1, null, Collections.emptyMap(), t);
    }

    public boolean isSuccess() {
        return HTTP_STATUS_2XX.contains(status);
    }
}
