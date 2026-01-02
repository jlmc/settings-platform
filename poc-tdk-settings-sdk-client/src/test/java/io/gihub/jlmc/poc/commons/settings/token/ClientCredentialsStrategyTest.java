package io.gihub.jlmc.poc.commons.settings.token;

import io.gihub.jlmc.poc.commons.settings.auth.ClientCredentials;
import io.gihub.jlmc.poc.commons.settings.exceptions.SettingsClientException;
import io.gihub.jlmc.poc.commons.settings.http.ClientHttpRequest;
import io.gihub.jlmc.poc.commons.settings.http.HttpExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientCredentialsStrategyTest {

    @Mock
    HttpExecutionStrategy httpExecutionStrategy;

    @Mock
    JsonDeserializer jsonDeserializer;

    ClientCredentialsStrategy strategy;

    ClientCredentials credentials;

    @BeforeEach
    void setUp() {
        strategy = new ClientCredentialsStrategy(httpExecutionStrategy, jsonDeserializer);

        credentials = new ClientCredentials(
                "client-id",
                "client-secret",
                "https://auth.example.com",
                List.of("read", "write")
        );
    }

    @Test
    void shouldReturnAccessToken_whenResponseIsValid() {
        // given
        when(httpExecutionStrategy.execute(any()))
                .thenReturn("{\"access_token\":\"token-123\"}");

        when(jsonDeserializer.deserialize(anyString(), eq(Map.class)))
                .thenReturn(Map.of("access_token", "token-123"));

        // when
        String token = strategy.acquireToken(credentials);

        // then
        assertThat(token).isEqualTo("token-123");
    }

    @Test
    void shouldThrowException_whenAccessTokenIsMissing() {
        when(httpExecutionStrategy.execute(any()))
                .thenReturn("{}");

        when(jsonDeserializer.deserialize(anyString(), eq(Map.class)))
                .thenReturn(Map.of());

        assertThatThrownBy(() -> strategy.acquireToken(credentials))
                .isInstanceOf(SettingsClientException.class)
                .hasMessageContaining("access_token");
    }

    @Test
    void shouldThrowException_whenAccessTokenIsBlank() {
        when(httpExecutionStrategy.execute(any()))
                .thenReturn("{\"access_token\":\" \"}");

        when(jsonDeserializer.deserialize(anyString(), eq(Map.class)))
                .thenReturn(Map.of("access_token", " "));

        assertThatThrownBy(() -> strategy.acquireToken(credentials))
                .isInstanceOf(SettingsClientException.class);
    }

    @Test
    void shouldPropagateHttpExecutionException() {
        RuntimeException error = new RuntimeException("connection error");

        when(httpExecutionStrategy.execute(any()))
                .thenThrow(error);

        assertThatThrownBy(() -> strategy.acquireToken(credentials))
                .isSameAs(error);
    }

    @Test
    void shouldBuildCorrectHttpRequest() {
        when(httpExecutionStrategy.execute(any()))
                .thenReturn("{\"access_token\":\"token\"}");

        when(jsonDeserializer.deserialize(anyString(), eq(Map.class)))
                .thenReturn(Map.of("access_token", "token"));

        ArgumentCaptor<ClientHttpRequest> captor =
                ArgumentCaptor.forClass(ClientHttpRequest.class);

        strategy.acquireToken(credentials);

        verify(httpExecutionStrategy).execute(captor.capture());

        ClientHttpRequest request = captor.getValue();

        assertThat(request.uri().toString())
                .isEqualTo("https://auth.example.com/oauth/token");

        assertThat(request.method().name()).isEqualTo("POST");

        assertThat(request.headers())
                .containsKey("Authorization");

        assertThat(request.body()).isNotNull();
    }

    @Test
    void shouldNotSendScope_whenScopesAreEmpty() {
        ClientCredentials noScopeCredentials = new ClientCredentials(
                "client-id",
                "client-secret",
                "https://auth.example.com",
                List.of()
        );

        when(httpExecutionStrategy.execute(any()))
                .thenReturn("{\"access_token\":\"token\"}");

        when(jsonDeserializer.deserialize(anyString(), eq(Map.class)))
                .thenReturn(Map.of("access_token", "token"));

        ArgumentCaptor<ClientHttpRequest> captor =
                ArgumentCaptor.forClass(ClientHttpRequest.class);

        strategy.acquireToken(noScopeCredentials);

        verify(httpExecutionStrategy).execute(captor.capture());

        ClientHttpRequest request = captor.getValue();

        assertThat(request.body().toString())
                .doesNotContain("scope");
    }
}
