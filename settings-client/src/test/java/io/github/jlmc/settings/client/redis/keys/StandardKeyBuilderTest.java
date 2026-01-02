package io.github.jlmc.settings.client.redis.keys;

import io.github.jlmc.settings.client.ConfigurationRequest;
import io.github.jlmc.settings.domain.entities.ConfigurationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StandardKeyBuilderTest {

    private static final String NAMESPACE = "settings";

    private Function<ConfigurationRequest, String> accountIdProvider;
    private StandardKeyBuilder keyBuilder;

    @BeforeEach
    void setup() {
        //noinspection unchecked
        accountIdProvider = mock(Function.class);
        keyBuilder = new StandardKeyBuilder(NAMESPACE, accountIdProvider);
    }

    @Test
    void build_shouldReturnFullKey_whenAllFieldsPresent() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn("my-service");
        when(request.objectType()).thenReturn(ConfigurationType.ACCOUNT);
        when(request.objectId()).thenReturn("obj123");
        when(accountIdProvider.apply(request)).thenReturn("account1");

        // Act
        String key = keyBuilder.build(request);

        // Assert
        assertThat(key).isEqualTo("settings:account1:my-service:ACCOUNT:obj123");
    }

    @Test
    void build_shouldReturnKeyWithoutObjectId_whenObjectIdIsNull() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn("my-service");
        when(request.objectType()).thenReturn(ConfigurationType.ACCOUNT);
        when(request.objectId()).thenReturn(null);
        when(accountIdProvider.apply(request)).thenReturn("account1");

        // Act
        String key = keyBuilder.build(request);

        // Assert
        assertThat(key).isEqualTo("settings:account1:my-service:ACCOUNT");
    }

    @Test
    void build_shouldReturnKeyWithoutObjectId_whenObjectIdIsBlank() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn("my-service");
        when(request.objectType()).thenReturn(ConfigurationType.ACCOUNT);
        when(request.objectId()).thenReturn("   "); // blank
        when(accountIdProvider.apply(request)).thenReturn("account1");

        // Act
        String key = keyBuilder.build(request);

        // Assert
        assertThat(key).isEqualTo("settings:account1:my-service:ACCOUNT");
    }

    @Test
    void build_shouldThrowException_whenServiceIsNull() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn(null);
        when(request.objectType()).thenReturn(ConfigurationType.ACCOUNT);
        when(accountIdProvider.apply(request)).thenReturn("account1");

        // Act & Assert
        assertThatThrownBy(() -> keyBuilder.build(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("service cannot be blank");
    }

    @Test
    void build_shouldThrowException_whenServiceIsBlank() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn("  ");
        when(request.objectType()).thenReturn(ConfigurationType.ACCOUNT);
        when(accountIdProvider.apply(request)).thenReturn("account1");

        // Act & Assert
        assertThatThrownBy(() -> keyBuilder.build(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("service cannot be blank");
    }

    @Test
    void build_shouldThrowException_whenAccountIdIsNull() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn("my-service");
        when(request.objectType()).thenReturn(ConfigurationType.ACCOUNT);
        when(accountIdProvider.apply(request)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> keyBuilder.build(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountId cannot be blank");
    }

    @Test
    void build_shouldThrowException_whenAccountIdIsBlank() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn("my-service");
        when(request.objectType()).thenReturn(ConfigurationType.ACCOUNT);
        when(accountIdProvider.apply(request)).thenReturn("  ");

        // Act & Assert
        assertThatThrownBy(() -> keyBuilder.build(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("accountId cannot be blank");
    }

    @Test
    void build_shouldHandleObjectIdWithSpaces() {
        // Arrange
        ConfigurationRequest request = mock(ConfigurationRequest.class);
        when(request.service()).thenReturn("my-service");
        when(request.objectType()).thenReturn(ConfigurationType.AGENT);
        when(request.objectId()).thenReturn("  objectId  "); // spaces
        when(accountIdProvider.apply(request)).thenReturn("account1");

        // Act
        String key = keyBuilder.build(request);

        // Assert
        assertThat(key).isEqualTo("settings:account1:my-service:AGENT:  objectId  "); // preserves spaces
    }
}
