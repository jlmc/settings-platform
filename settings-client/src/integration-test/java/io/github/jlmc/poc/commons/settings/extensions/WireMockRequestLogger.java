package io.github.jlmc.poc.commons.settings.extensions;

import com.github.tomakehurst.wiremock.extension.requestfilter.RequestFilterAction;
import com.github.tomakehurst.wiremock.extension.requestfilter.StubRequestFilter;
import com.github.tomakehurst.wiremock.http.Request;

import java.util.stream.Collectors;

/// WireMock request filter that logs incoming requests to stdout
public class WireMockRequestLogger extends StubRequestFilter {

    @Override
    public String getName() {
        return "header-logger";
    }

    @Override
    public RequestFilterAction filter(Request request) {
        String headers = request.getHeaders().all().stream()
                .map(h -> h.key() + ": " + String.join(",", h.values()))
                .collect(Collectors.joining(System.lineSeparator()));

        String body = request.getBodyAsString();

        StringBuilder logMessage = new StringBuilder()
                .append("---- WireMock Request ----")
                .append(System.lineSeparator()).append(request.getMethod()).append(" ").append(request.getUrl())
                .append(System.lineSeparator())
                .append(headers)
                .append(System.lineSeparator());

        if (body != null && !body.isBlank()) {
            logMessage.append(System.lineSeparator()).append(body).append(System.lineSeparator()).append(System.lineSeparator());
        }

        logMessage.append("---- End WireMock Request ----").append(System.lineSeparator());

        System.out.println(logMessage);

        return RequestFilterAction.continueWith(request);
    }
}
