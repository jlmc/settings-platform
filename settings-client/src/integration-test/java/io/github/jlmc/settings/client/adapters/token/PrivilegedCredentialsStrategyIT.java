package io.github.jlmc.settings.client.adapters.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.jlmc.settings.client.adapters.http.ClientHttpExecutor;
import io.github.jlmc.settings.client.adapters.http.DefaultClientHttpExecutor;
import io.github.jlmc.settings.client.adapters.http.HttpConstants;
import io.github.jlmc.settings.client.adapters.json.JacksonJsonDeserializer;
import io.github.jlmc.settings.client.adapters.resilience.ResilientHttpExecutionStrategy;
import io.github.jlmc.settings.client.core.auth.PrivilegedCredentials;
import io.github.jlmc.settings.client.core.exceptions.DeserializationSettingsClientException;
import io.github.jlmc.settings.client.core.exceptions.SettingsClientException;
import io.github.jlmc.settings.client.core.exceptions.SettingsClientHttpException;
import io.github.jlmc.settings.client.extensions.WireMockRequestLogger;
import io.github.jlmc.settings.client.extensions.WireMockResponseLogger;
import io.github.jlmc.settings.client.ports.out.JsonDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.net.http.HttpClient;
import java.net.http.HttpTimeoutException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.Base64;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrivilegedCredentialsStrategyIT {

    private static final String CLIENT_ID = "privileged-client";
    private static final String KEY_ID = "key-001";

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig()
                    .dynamicPort()
                    .extensions(new WireMockRequestLogger(), new WireMockResponseLogger())
            )
            .build();

    private PrivilegedCredentialsStrategy victim;

    private ClientHttpExecutor clientHttpExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    private JsonDeserializer jsonDeserializer;
    private String privateKeyB64;

    @AfterEach
    void tearDown() throws Exception {
        this.clientHttpExecutor.close();
    }

    @BeforeEach
    void setup() throws Exception {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        this.clientHttpExecutor = new DefaultClientHttpExecutor(
                Duration.ofSeconds(2),
                Duration.ofSeconds(2),
                "it-test-client/1.0",
                httpClient
        );

        jsonDeserializer = new JacksonJsonDeserializer(objectMapper);

        ResilientHttpExecutionStrategy httpExecutionStrategy =
                new ResilientHttpExecutionStrategy(clientHttpExecutor);

        this.victim = new PrivilegedCredentialsStrategy(httpExecutionStrategy, jsonDeserializer);

        // Generate an ephemeral EC private key for test JWTs
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        privateKeyB64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    private void stubTokenEndpoint(int status, String body) {
        stubTokenEndpoint(status, body, null);
    }

    private void stubTokenEndpoint(int status, String body, Integer delayMs) {
        var responseBuilder =
                aResponse()
                        .withStatus(status)
                        .withHeader(HttpConstants.HEADER_CONTENT_TYPE, "application/json")
                        .withBody(body);

        if (delayMs != null) {
            responseBuilder.withFixedDelay(delayMs);
        }

        wireMock.stubFor(
                post(urlEqualTo("/oauth/token"))
                        .withHeader(
                                HttpConstants.HEADER_CONTENT_TYPE,
                                equalTo(HttpConstants.CONTENT_TYPE_FORM)
                        )
                        .willReturn(responseBuilder)
        );
    }

    private PrivilegedCredentials buildCredentials() {
        return new PrivilegedCredentials(
                CLIENT_ID,
                KEY_ID,
                privateKeyB64,
                wireMock.baseUrl()
        );
    }

    private void verifyHttpRequest() {
        wireMock.verify(
                postRequestedFor(urlEqualTo("/oauth/token"))
                        .withHeader(
                                HttpConstants.HEADER_CONTENT_TYPE,
                                equalTo(HttpConstants.CONTENT_TYPE_FORM)
                        )
                        .withRequestBody(containing("grant_type=client_credentials"))
                        .withRequestBody(
                                containing(
                                        "client_assertion_type=urn%3Aietf%3Aparams%3Aoauth%3Aclient-assertion-type%3Ajwt-bearer"
                                )
                        )
                        .withRequestBody(containing("client_id=" + CLIENT_ID))
        );
    }

    @Test
    void shouldSuccessfullyAcquireToken() {
        String expectedToken = "TOKEN_PRIV_ABC";
        String body = """
                {
                "access_token": "%s",
                "expires_in": 3600,
                "token_type": "Bearer"
                }""".formatted(expectedToken);

        stubTokenEndpoint(200, body);

        String token = victim.acquireToken(buildCredentials());

        assertEquals(expectedToken, token);
        verifyHttpRequest();
    }

    @Test
    void shouldThrowSettingsClientHttpExceptionOnNon2xxResponse() {
        String errorBody = """
                {"error":"invalid_client"}
                """;
        stubTokenEndpoint(401, errorBody);

        SettingsClientHttpException ex =
                assertThrows(
                        SettingsClientHttpException.class,
                        () -> victim.acquireToken(buildCredentials())
                );

        assertEquals(401, ex.getStatusCode());
        assertTrue(ex.getMessage().contains(errorBody));
        verifyHttpRequest();
    }


    @Test
    void shouldThrowDeserializationSettingsClientExceptionOnInvalidJson() {
        stubTokenEndpoint(200, "{bad_json}");

        DeserializationSettingsClientException ex =
                assertThrows(
                        DeserializationSettingsClientException.class,
                        () -> victim.acquireToken(buildCredentials())
                );

        assertTrue(ex.getMessage().contains("JSON deserialization"));
        verifyHttpRequest();
    }

    @Test
    void shouldThrowSettingsClientExceptionOnTimeout() {
        stubTokenEndpoint(200, "{\"access_token\":\"abc\"}", 5000);

        DefaultClientHttpExecutor timeoutClient =
                new DefaultClientHttpExecutor(
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(1),
                        "it-timeout-test-client",
                        null
                );

        ResilientHttpExecutionStrategy httpExecutionStrategy =
                new ResilientHttpExecutionStrategy(timeoutClient);

        PrivilegedCredentialsStrategy timeoutVictim =
                new PrivilegedCredentialsStrategy(httpExecutionStrategy, jsonDeserializer);

        SettingsClientException ex =
                assertThrows(
                        SettingsClientException.class,
                        () -> timeoutVictim.acquireToken(buildCredentials())
                );

        assertInstanceOf(HttpTimeoutException.class, ex.getCause());
        verifyHttpRequest();
    }

    @Test
    void shouldThrowSettingsClientExceptionIfAccessTokenIsMissing() {
        stubTokenEndpoint(200, "{\"expires_in\":3600}");

        SettingsClientException ex =
                assertThrows(
                        SettingsClientException.class,
                        () -> victim.acquireToken(buildCredentials())
                );

        assertEquals("Missing access_token in token response", ex.getMessage());
        verifyHttpRequest();
    }
}
