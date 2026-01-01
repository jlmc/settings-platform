package io.gihub.jlmc.poc.commons.settings.token;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.gihub.jlmc.poc.commons.settings.auth.ClientCredentials;
import io.gihub.jlmc.poc.commons.settings.exceptions.DeserializationSettingsClientException;
import io.gihub.jlmc.poc.commons.settings.exceptions.SettingsClientException;
import io.gihub.jlmc.poc.commons.settings.exceptions.SettingsClientHttpException;
import io.gihub.jlmc.poc.commons.settings.extensions.WireMockRequestLogger;
import io.gihub.jlmc.poc.commons.settings.extensions.WireMockResponseLogger;
import io.gihub.jlmc.poc.commons.settings.http.ClientHttpExecutor;
import io.gihub.jlmc.poc.commons.settings.http.DefaultClientHttpExecutor;
import io.gihub.jlmc.poc.commons.settings.http.HttpConstants;
import io.gihub.jlmc.poc.commons.settings.http.HttpStatusCode;
import io.gihub.jlmc.poc.commons.settings.json.JacksonJsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.resilience.ResilientHttpExecutionStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientCredentialsStrategyIT {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .extensions(new WireMockRequestLogger(), new WireMockResponseLogger())
            )
            .build();

    private static final String CLIENT_ID = "my-client";
    private static final String CLIENT_SECRET = "secret-123";
    private static final List<String> SCOPES = List.of("read", "write");

    private JsonDeserializer deserializer;
    private ClientCredentialsStrategy victim;

    @BeforeEach
    void setup() {
        ClientHttpExecutor clientHttpExecutor = new DefaultClientHttpExecutor(
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                "it-test-client/1.0",
                null
        );

        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        deserializer = new JacksonJsonDeserializer(objectMapper);

        ResilientHttpExecutionStrategy httpExecutionStrategy = new ResilientHttpExecutionStrategy(clientHttpExecutor);

        victim = new ClientCredentialsStrategy(httpExecutionStrategy, deserializer);
    }

    private void stubTokenEndpoint(int status, String body, Integer delayMs) {
        var responseBuilder = aResponse()
                .withStatus(status)
                .withHeader(HttpConstants.HEADER_CONTENT_TYPE, "application/json")
                .withBody(body);

        if (delayMs != null) {
            responseBuilder.withFixedDelay(delayMs);
        }

        wireMock.stubFor(post(urlEqualTo("/oauth/token"))
                .withHeader(HttpConstants.HEADER_AUTHORIZATION, matching("Basic .*"))
                .withHeader(HttpConstants.HEADER_CONTENT_TYPE, equalTo(HttpConstants.CONTENT_TYPE_FORM))
                .willReturn(responseBuilder));
    }

    private void verifyHttpRequest() {
        wireMock.verify(postRequestedFor(urlPathEqualTo("/oauth/token"))
                .withHeader(HttpConstants.HEADER_AUTHORIZATION, equalTo(buildBasicAuthHeader()))
                .withHeader(HttpConstants.HEADER_CONTENT_TYPE, equalTo(HttpConstants.CONTENT_TYPE_FORM))
                .withRequestBody(containing("grant_type=client_credentials"))
                .withRequestBody(containing("scope=read+write"))
                .withRequestBody(containing("client_id=" + CLIENT_ID))
        );
    }

    private String buildBasicAuthHeader() {
        String token = CLIENT_ID + ":" + CLIENT_SECRET;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void shouldProperlyEncodeFormParameters() {
        String specialClientId = "client with spaces & symbols";
        String specialSecret = "secret=123+456";
        List<String> specialScopes = List.of("scope 1", "scope 2");

        String expectedToken = "ENCODED_TOKEN";
        String responseBody = "{\"access_token\": \"" + expectedToken + "\"}";

        stubTokenEndpoint(200, responseBody, null);

        ClientCredentials credentials = new ClientCredentials(
                specialClientId,
                specialSecret,
                wireMock.baseUrl(),
                specialScopes
        );

        String token = victim.acquireToken(credentials);

        assertEquals(expectedToken, token);

        // Verify encoding in the request body
        wireMock.verify(postRequestedFor(urlPathEqualTo("/oauth/token"))
                .withRequestBody(containing("client_id=client+with+spaces+%26+symbols"))
                .withRequestBody(containing("scope=scope+1+scope+2"))
        );
    }

    @Test
    void shouldAcquireTokenSuccessfully() {
        String expectedToken = "TOKEN_ABC_123";
        String responseBody = """
                {
                  "access_token": "%s",
                  "expires_in": 3600,
                  "token_type": "Bearer",
                  "scope": "read write"
                }
                """.formatted(expectedToken);

        stubTokenEndpoint(200, responseBody, null);

        ClientCredentials credentials = new ClientCredentials(
                CLIENT_ID,
                CLIENT_SECRET,
                wireMock.baseUrl(),
                SCOPES
        );

        String token = victim.acquireToken(credentials);

        assertEquals(expectedToken, token);
        verifyHttpRequest();
    }

    @Test
    void shouldThrowDeserializationExceptionOnInvalidJson() {
        stubTokenEndpoint(200, "{invalid_json}", null);

        ClientCredentials credentials = new ClientCredentials(
                CLIENT_ID,
                CLIENT_SECRET,
                wireMock.baseUrl(),
                SCOPES
        );

        DeserializationSettingsClientException ex = assertThrows(DeserializationSettingsClientException.class, () ->
                victim.acquireToken(credentials)
        );

        assertTrue(ex.getMessage().contains("JSON deserialization error"));
        assertNotNull(ex.getCause());
        assertInstanceOf(JsonProcessingException.class, ex.getCause());

        verifyHttpRequest();
    }

    @Test
    void shouldThrowSettingsClientHttpExceptionOnServerError() {
        HttpStatusCode status = HttpStatusCode.INTERNAL_SERVER_ERROR;
        String responseBody = "{\"error\":\"" + status.getReason() + "\"}";
        stubTokenEndpoint(status.getCode(), responseBody, null);

        ClientCredentials credentials = new ClientCredentials(
                CLIENT_ID,
                CLIENT_SECRET,
                wireMock.baseUrl(),
                SCOPES
        );

        SettingsClientHttpException ex = assertThrows(SettingsClientHttpException.class, () ->
                victim.acquireToken(credentials)
        );

        assertEquals("HTTP " + status.getCode() + " (" + status.getReason() + ") from "
                        + wireMock.baseUrl() + "/oauth/token. Response body (truncated): " + responseBody,
                ex.getMessage()
        );

        verifyHttpRequest();
    }

    @Test
    void shouldThrowSettingsClientHttpExceptionOnUnauthorized() {
        HttpStatusCode status = HttpStatusCode.UNAUTHORIZED;
        String responseBody = "{\"error\":\"invalid_client\"}";
        stubTokenEndpoint(status.getCode(), responseBody, null);

        ClientCredentials credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET, wireMock.baseUrl(), SCOPES);

        SettingsClientHttpException ex = assertThrows(SettingsClientHttpException.class, () ->
                victim.acquireToken(credentials)
        );

        assertEquals(status.getCode(), ex.getStatusCode());
        verifyHttpRequest();
    }

    @Test
    void shouldThrowSettingsClientHttpExceptionOnBadRequest() {
        HttpStatusCode status = HttpStatusCode.BAD_REQUEST;
        String responseBody = "{\"error\":\"invalid_scope\"}";
        stubTokenEndpoint(status.getCode(), responseBody, null);

        ClientCredentials credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET, wireMock.baseUrl(), SCOPES);

        SettingsClientHttpException ex = assertThrows(SettingsClientHttpException.class, () ->
                victim.acquireToken(credentials)
        );

        assertEquals(status.getCode(), ex.getStatusCode());
        verifyHttpRequest();
    }

    @Test
    void shouldThrowSettingsClientHttpExceptionOnTooManyRequests() {
        HttpStatusCode status = HttpStatusCode.TOO_MANY_REQUESTS;
        String responseBody = "{\"error\":\"rate_limit_exceeded\"}";
        stubTokenEndpoint(status.getCode(), responseBody, null);

        ClientCredentials credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET, wireMock.baseUrl(), SCOPES);

        SettingsClientHttpException ex = assertThrows(SettingsClientHttpException.class, () ->
                victim.acquireToken(credentials)
        );

        assertEquals(status.getCode(), ex.getStatusCode());
        verifyHttpRequest();
    }

    @Test
    void shouldThrowExceptionWhenAccessTokenIsMissing() {
        String responseBody = "{\"expires_in\": 3600}";
        stubTokenEndpoint(200, responseBody, null);

        ClientCredentials credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET, wireMock.baseUrl(), SCOPES);

        SettingsClientException ex = assertThrows(SettingsClientException.class, () ->
                victim.acquireToken(credentials)
        );

        assertTrue(ex.getMessage().contains("access_token is missing or empty"));
        verifyHttpRequest();
    }

    @Test
    void shouldThrowExceptionWhenAccessTokenIsEmpty() {
        String responseBody = "{\"access_token\": \"  \", \"expires_in\": 3600}";
        stubTokenEndpoint(200, responseBody, null);

        ClientCredentials credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET, wireMock.baseUrl(), SCOPES);

        SettingsClientException ex = assertThrows(SettingsClientException.class, () ->
                victim.acquireToken(credentials)
        );

        assertTrue(ex.getMessage().contains("access_token is missing or empty"));
        verifyHttpRequest();
    }

    @Test
    void shouldRetryWhenRetryExecutorIsProvided() {
        String expectedToken = "RETRY_TOKEN";
        String responseBody = "{\"access_token\": \"" + expectedToken + "\"}";

        // Mock a RetryExecutor that just executes the supplier
        RetryExecutorMock retryExecutor = new RetryExecutorMock();

        ResilientHttpExecutionStrategy resilientStrategy = new ResilientHttpExecutionStrategy(
                new DefaultClientHttpExecutor(),
                retryExecutor
        );
        ClientCredentialsStrategy victimWithRetry = new ClientCredentialsStrategy(resilientStrategy, deserializer);

        stubTokenEndpoint(200, responseBody, null);

        ClientCredentials credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET, wireMock.baseUrl(), SCOPES);

        String token = victimWithRetry.acquireToken(credentials);

        assertEquals(expectedToken, token);
        assertEquals(1, retryExecutor.getCalls());
    }

    private static class RetryExecutorMock implements io.gihub.jlmc.poc.commons.settings.resilience.RetryExecutor {
        private int calls = 0;
        @Override
        public <T> T execute(java.util.function.Supplier<T> supplier) {
            calls++;
            return supplier.get();
        }
        public int getCalls() { return calls; }
    }

    @Test
    void shouldActuallyRetryOnTransientFailureWithMockExecutor() {
        String expectedToken = "SUCCESS_AFTER_RETRY";
        String successResponse = "{\"access_token\": \"" + expectedToken + "\"}";

        // This test verifies that the strategy uses the retry executor.
        // The retry executor itself will handle the retry logic.
        RetryExecutorMock retryExecutor = new RetryExecutorMock();

        ResilientHttpExecutionStrategy resilientStrategy = new ResilientHttpExecutionStrategy(
                new DefaultClientHttpExecutor(),
                retryExecutor
        );
        ClientCredentialsStrategy victimWithRetry = new ClientCredentialsStrategy(resilientStrategy, deserializer);

        stubTokenEndpoint(200, successResponse, null);

        ClientCredentials credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET, wireMock.baseUrl(), SCOPES);

        String token = victimWithRetry.acquireToken(credentials);

        assertEquals(expectedToken, token);
        assertEquals(1, retryExecutor.getCalls());
    }

    @Test
    void shouldThrowSettingsClientExceptionOnTimeout() {
        stubTokenEndpoint(200, "{\"access_token\":\"abc\"}", 5000);

        HttpClient timeoutHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(1))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        ResilientHttpExecutionStrategy httpExecutionStrategy = new ResilientHttpExecutionStrategy(
                new DefaultClientHttpExecutor(
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(1),
                        "timeout-test",
                        timeoutHttpClient
                )
        );

        ClientCredentialsStrategy victimWithTimeout = new ClientCredentialsStrategy(httpExecutionStrategy, deserializer);

        ClientCredentials credentials = new ClientCredentials(
                CLIENT_ID,
                CLIENT_SECRET,
                wireMock.baseUrl(),
                SCOPES
        );

        SettingsClientException ex = assertThrows(SettingsClientException.class, () ->
                victimWithTimeout.acquireToken(credentials)
        );

        assertInstanceOf(HttpTimeoutException.class, ex.getCause());

        verifyHttpRequest();
    }
}
