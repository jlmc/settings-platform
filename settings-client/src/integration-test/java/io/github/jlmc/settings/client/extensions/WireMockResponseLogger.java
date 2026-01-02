package io.github.jlmc.settings.client.extensions;


import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

/// WireMock response transformer that logs outgoing responses to stdout
public class WireMockResponseLogger implements ResponseDefinitionTransformerV2 {

    @Override
    public String getName() {
        return "response-logger";
    }

    @Override
    public ResponseDefinition transform(ServeEvent serveEvent) {
        LoggedRequest request = serveEvent.getRequest();
        ResponseDefinition responseDefinition = serveEvent.getResponseDefinition();

        StringBuilder logMessage = new StringBuilder()
                .append("---- WireMock Response ----")
                .append(System.lineSeparator())
                .append(request.getMethod()).append(" ").append(request.getUrl())
                .append(System.lineSeparator())
                .append("Status: ").append(responseDefinition.getStatus())
                .append(System.lineSeparator());

        String body = responseDefinition.getBody();
        if (body != null && !body.isBlank()) {
            logMessage.append(System.lineSeparator()).append(body).append(System.lineSeparator()).append(System.lineSeparator());
        }

        logMessage.append("---- End WireMock Response ----").append(System.lineSeparator());

        System.out.println(logMessage);

        // Return original response unchanged
        return responseDefinition;
    }

    @Override
    public boolean applyGlobally() {
        return true; // Apply to all responses
    }
}
