package io.github.jlmc.poc.commons.settings.token;

import io.github.jlmc.poc.commons.settings.auth.ClientCredentials;
import io.github.jlmc.poc.commons.settings.exceptions.SettingsClientException;
import io.github.jlmc.poc.commons.settings.http.Body;
import io.github.jlmc.poc.commons.settings.http.ClientHttpRequest;
import io.github.jlmc.poc.commons.settings.http.HttpConstants;
import io.github.jlmc.poc.commons.settings.http.HttpExecutionStrategy;
import io.github.jlmc.poc.commons.settings.http.HttpMethod;
import io.github.jlmc.poc.commons.settings.http.UrlBuilder;
import io.github.jlmc.poc.commons.settings.json.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/// OAuth2 Token acquisition strategy using client credentials.
///
/// Requests a token from the OAuth2 server using the client credentials flow.
public class ClientCredentialsStrategy implements TokenAcquisitionStrategy<ClientCredentials> {

    private static final Logger logger = LoggerFactory.getLogger(ClientCredentialsStrategy.class);
    private static final String GRANT_TYPE = "client_credentials";

    private final HttpExecutionStrategy httpExecutionStrategy;
    private final JsonDeserializer jsonDeserializer;

    public ClientCredentialsStrategy(HttpExecutionStrategy httpExecutionStrategy,
                                     JsonDeserializer jsonDeserializer) {
        this.httpExecutionStrategy = httpExecutionStrategy;
        this.jsonDeserializer = jsonDeserializer;
    }

    @Override
    public Class<ClientCredentials> getSupportedType() {
        return ClientCredentials.class;
    }

    @Override
    public String acquireToken(ClientCredentials credentials) {
        ClientHttpRequest request = buildRequest(credentials);
        String responseBody = executeHttpCall(request);
        return parseAccessToken(responseBody);
    }

    private String parseAccessToken(String source) {
        @SuppressWarnings("unchecked")
        Map<String, ?> map = jsonDeserializer.deserialize(source, Map.class);

        //   String access_token,
        //   int expires_in,
        //   String token_type,
        //   String scope
        Object token = map.get("access_token");

        if (!(token instanceof String accessToken) || accessToken.isBlank()) {
            throw new SettingsClientException("Invalid OAuth token response: access_token is missing or empty");
        }

        return accessToken;
    }

    private String executeHttpCall(ClientHttpRequest request) {
        return httpExecutionStrategy.execute(request);
    }

    private ClientHttpRequest buildRequest(ClientCredentials credentials) {
        URI uri = UrlBuilder.create()
                .withBasePath(credentials.tokenUrl())
                .path("oauth")
                .path("token")
                .toURI();

        logger.debug("Requesting OAuth2 token for clientId={}", credentials.clientId());

        String scope = credentials.scopes().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));

        Map<String, String> formData = new HashMap<>();
        formData.put("grant_type", GRANT_TYPE);
        formData.put("client_id", credentials.clientId());
        if (!scope.isBlank()) {
            formData.put("scope", scope);
        }

        Body.Form form = new Body.Form(formData);

        return new ClientHttpRequest(
                uri,
                HttpMethod.POST,
                Map.of(HttpConstants.HEADER_AUTHORIZATION, buildBasicAuthHeader(credentials)),
                null,
                form
        );
    }

    private String buildBasicAuthHeader(ClientCredentials credentials) {
        String token = credentials.clientId() + ":" + credentials.clientSecret();
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }
}
