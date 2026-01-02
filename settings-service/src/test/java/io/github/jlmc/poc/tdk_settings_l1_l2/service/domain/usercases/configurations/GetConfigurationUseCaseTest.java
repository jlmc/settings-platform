package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.inputs.ResolveConfigurationInput;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services.ConfigurationDecryptionService;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services.ConfigurationResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetConfigurationUseCase Unit Tests")
class GetConfigurationUseCaseTest {

    private static final String ACCOUNT_ID = "acc-1";
    private static final String SERVICE_NAME = "srv-1";
    private static final String PRIVATE_KEY = "private-key";

    @Mock
    private ConfigurationResolver configurationResolver;
    @Mock
    private ConfigurationDecryptionService configurationDecryptionService;

    @InjectMocks
    private GetConfigurationUseCase victim;

    @Test
    @DisplayName("Should return merged settings directly when no private key is provided in input")
    void executeReturnsMergedSettingsWhenNoPrivateKey() {
        ResolveConfigurationInput input = createInput(ConfigurationType.ACCOUNT, null);
        Map<String, Object> mergedSettings = Map.of("key", "value");
        ResolvedConfiguration resolved = new ResolvedConfiguration(
                ACCOUNT_ID, SERVICE_NAME, ConfigurationType.ACCOUNT, null, List.of(), mergedSettings
        );

        when(configurationResolver.resolve(input)).thenReturn(resolved);

        Map<String, Object> result = victim.execute(input);

        assertEquals(mergedSettings, result);
        verify(configurationResolver).resolve(input);
        verifyNoInteractions(configurationDecryptionService);
    }

    @Test
    @DisplayName("Should return decrypted settings when private key is provided in input")
    void executeReturnsDecryptedSettingsWhenPrivateKeyIsProvided() {
        ResolveConfigurationInput input = createInput(ConfigurationType.ACCOUNT, PRIVATE_KEY);
        Map<String, Object> mergedSettings = Map.of("key", "encrypted");
        ResolvedConfiguration resolved = new ResolvedConfiguration(
                ACCOUNT_ID, SERVICE_NAME, ConfigurationType.ACCOUNT, null, List.of(), mergedSettings
        );
        Map<String, Object> decryptedSettings = Map.of("key", "decrypted");

        when(configurationResolver.resolve(input)).thenReturn(resolved);
        when(configurationDecryptionService.decryptForReturn(input, resolved)).thenReturn(decryptedSettings);

        Map<String, Object> result = victim.execute(input);

        assertEquals(decryptedSettings, result);
        verify(configurationResolver).resolve(input);
        verify(configurationDecryptionService).decryptForReturn(input, resolved);
    }

    private ResolveConfigurationInput createInput(ConfigurationType type, String privateKey) {
        return new ResolveConfigurationInput(ACCOUNT_ID, SERVICE_NAME, type, privateKey);
    }
}
