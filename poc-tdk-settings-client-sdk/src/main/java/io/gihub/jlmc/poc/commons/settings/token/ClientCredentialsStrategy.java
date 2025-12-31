package io.gihub.jlmc.poc.commons.settings.token;

import io.gihub.jlmc.poc.commons.settings.auth.ClientCredentials;
import io.gihub.jlmc.poc.commons.settings.http.Body;
import io.gihub.jlmc.poc.commons.settings.http.ClientHttpRequest;
import io.gihub.jlmc.poc.commons.settings.http.HttpConstants;
import io.gihub.jlmc.poc.commons.settings.http.HttpExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.http.HttpMethod;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OAuth2 Token acquisition strategy using client credentials.
 *
 * Requests a token from the OAuth2 server using the client credentials flow.
 */
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
        AccessTokenResponse response = jsonDeserializer.deserialize(source, AccessTokenResponse.class);
        return response.access_token();
    }

    private String executeHttpCall(ClientHttpRequest request) {
        return httpExecutionStrategy.execute(request);
    }

    private ClientHttpRequest buildRequest(ClientCredentials credentials) {
        URI authUri = URI.create(credentials.tokenUrl() + "/oauth/token");
        logger.info("Requesting OAuth2 token for clientId={}", credentials.clientId());

        String scope = credentials.scopes().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" "));


        Body.Form form = new Body.Form(
                Map.of(
                        "grant_type", GRANT_TYPE,
                        "client_id", credentials.clientId(),
                        "scope", scope
                )
        );


        return new ClientHttpRequest(
                authUri,
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

    /** Response data model for OAuth2 token endpoint. */
    public record AccessTokenResponse(
            String access_token,
            int expires_in,
            String token_type,
            String scope
    ) {}
}
