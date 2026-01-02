package io.github.jlmc.settings.client.token;

import io.github.jlmc.settings.client.auth.BearerTokenCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("BearerTokenStrategy")
class BearerTokenStrategyTest {

    private BearerTokenStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new BearerTokenStrategy();
    }

    @Test
    @DisplayName("should expose BearerTokenCredentials as supported type")
    void shouldExposeSupportedType() {
        // when
        Class<?> supportedType = strategy.getSupportedType();

        // then
        assertThat(supportedType)
                .isEqualTo(BearerTokenCredentials.class);
    }

    @Test
    @DisplayName("should return the same token provided in credentials")
    void shouldReturnProvidedToken() {
        // given
        BearerTokenCredentials credentials =
                new BearerTokenCredentials("test-bearer-token");

        // when
        String token = strategy.acquireToken(credentials);

        // then
        assertThat(token)
                .isEqualTo("test-bearer-token");
    }

    @Test
    @DisplayName("should not modify or transform the token")
    void shouldNotModifyToken() {
        // given
        String originalToken = "Bearer-XYZ_123.with-special_chars";
        BearerTokenCredentials credentials =
                new BearerTokenCredentials(originalToken);

        // when
        String token = strategy.acquireToken(credentials);

        // then
        assertThat(token)
                .isSameAs(originalToken); // same reference, no transformation
    }

    @Test
    @DisplayName("should throw NullPointerException when credentials are null")
    void shouldFailFastWhenCredentialsAreNull() {
        // expect
        assertThatThrownBy(() -> strategy.acquireToken(null))
                .isInstanceOf(NullPointerException.class);
    }
}
