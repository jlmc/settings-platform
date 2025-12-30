package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.Rsa;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ObjectNodeMerger;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountDecrypter;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
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
    private SettingsAccountRepository settingsAccountRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ObjectNodeMerger objectNodeMerger;
    @Mock
    private ServiceJsonSchemasRepository serviceJsonSchemasRepository;
    @Mock
    private SettingsAccountDecrypter settingsAccountDecrypter;

    @InjectMocks
    private GetConfigurationUseCase victim;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    @DisplayName("Execution Scenarios")
    class ExecutionScenarios {

        @Test
        @DisplayName("Should throw NotFoundException when no settings are found in repository")
        void executeWithNoSettingsFoundThrowsNotFoundException() {
            Input input = createInput(ConfigurationType.ACCOUNT, null);
            when(settingsAccountRepository.findAll(ACCOUNT_ID, SERVICE_NAME)).thenReturn(Collections.emptyList());

            assertThrows(NotFoundException.class, () -> victim.execute(input));

            verifyNoInteractions(eventPublisher, objectNodeMerger, serviceJsonSchemasRepository, settingsAccountDecrypter);
        }

        @Test
        @DisplayName("Should throw NotFoundException when settings exist but are above the priority threshold")
        void executeWithNoSettingsWithinThresholdThrowsNotFoundException() {
            Input input = createInput(ConfigurationType.SERVICE, null);
            SettingsAccount accountSetting = createSettingsAccount(ConfigurationType.ACCOUNT, objectMapper.createObjectNode());
            when(settingsAccountRepository.findAll(ACCOUNT_ID, SERVICE_NAME)).thenReturn(List.of(accountSetting));

            assertThrows(NotFoundException.class, () -> victim.execute(input));
        }

        @Test
        @DisplayName("Should return merged settings without decryption when no private key is provided")
        void executeWithSettingsAndNoDecryptionDueToMissingPrivateKey() {
            Input input = createInput(ConfigurationType.ACCOUNT, null);
            ObjectNode content = objectMapper.createObjectNode().put("key", "value");
            SettingsAccount setting = createSettingsAccount(ConfigurationType.ACCOUNT, content);

            when(settingsAccountRepository.findAll(ACCOUNT_ID, SERVICE_NAME)).thenReturn(List.of(setting));
            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(Map.of("key", "value"));

            Map<String, Object> result = victim.execute(input);

            assertEquals("value", result.get("key"));
            verify(serviceJsonSchemasRepository, times(1)).findByServiceName(input.serviceName());
            verifyNoInteractions(settingsAccountDecrypter);
            verify(eventPublisher).publishEvent(any(ConfigurationHitEvent.class));
        }

        @Test
        @DisplayName("Should return merged settings without decryption when no schema is found for the service")
        void executeWithSettingsAndNoDecryptionDueToMissingSchema() {
            Input input = createInput(ConfigurationType.ACCOUNT, PRIVATE_KEY);
            ObjectNode content = objectMapper.createObjectNode().put("key", "value");
            SettingsAccount setting = createSettingsAccount(ConfigurationType.ACCOUNT, content);

            when(settingsAccountRepository.findAll(ACCOUNT_ID, SERVICE_NAME)).thenReturn(List.of(setting));
            when(serviceJsonSchemasRepository.findByServiceName(SERVICE_NAME)).thenReturn(Optional.empty());
            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(Map.of("key", "value"));

            Map<String, Object> result = victim.execute(input);

            assertEquals("value", result.get("key"));
            verifyNoInteractions(settingsAccountDecrypter);
            verify(eventPublisher).publishEvent(any(ConfigurationHitEvent.class));
        }

        @Test
        @DisplayName("Should return decrypted and merged settings when private key and schema are available")
        void executeWithSettingsAndDecryption() {
            Input input = createInput(ConfigurationType.ACCOUNT, PRIVATE_KEY);
            ObjectNode content = objectMapper.createObjectNode().put("key", "encrypted");
            SettingsAccount setting = createSettingsAccount(ConfigurationType.ACCOUNT, content);

            JsonSchema jsonSchema = new JsonSchema(ConfigurationType.ACCOUNT, objectMapper.createObjectNode().put("type", "object"));
            ServiceJsonSchemas schemas = new ServiceJsonSchemas(SERVICE_NAME, List.of(jsonSchema), new Rsa("public-key"));

            when(settingsAccountRepository.findAll(ACCOUNT_ID, SERVICE_NAME)).thenReturn(List.of(setting));
            when(serviceJsonSchemasRepository.findByServiceName(SERVICE_NAME)).thenReturn(Optional.of(schemas));

            ObjectNode decryptedContent = objectMapper.createObjectNode().put("key", "decrypted");
            SettingsAccount decryptedSetting = createSettingsAccount(ConfigurationType.ACCOUNT, decryptedContent);

            when(settingsAccountDecrypter.decryptConfigurationJsons(any(), eq(schemas), eq(PRIVATE_KEY)))
                    .thenReturn(List.of(decryptedSetting));

            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(Map.of("key", "decrypted"));

            Map<String, Object> result = victim.execute(input);

            assertEquals("decrypted", result.get("key"));
            verify(settingsAccountDecrypter).decryptConfigurationJsons(any(), eq(schemas), eq(PRIVATE_KEY));
            verify(eventPublisher).publishEvent(any(ConfigurationHitEvent.class));
        }
    }

    @Nested
    @DisplayName("Internal Logic Verification")
    class InternalLogicVerification {

        @Test
        @DisplayName("Should verify that settings are merged in ascending order of priority")
        void executeVerifiesSortingByPriority() {
            Input input = createInput(ConfigurationType.AGENT, null);
            SettingsAccount serviceSetting = createSettingsAccount(ConfigurationType.SERVICE, objectMapper.createObjectNode().put("src", "service"));
            SettingsAccount accountSetting = createSettingsAccount(ConfigurationType.ACCOUNT, objectMapper.createObjectNode().put("src", "account"));

            // Return them in reverse order of priority (ACCOUNT then SERVICE)
            when(settingsAccountRepository.findAll(ACCOUNT_ID, SERVICE_NAME)).thenReturn(List.of(accountSetting, serviceSetting));

            victim.execute(input);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Supplier<ObjectNode>>> captor = ArgumentCaptor.forClass(List.class);
            // It's called once in the use case because no decryption happens
            verify(objectNodeMerger, times(1)).mergeContentsAsMap(captor.capture());

            List<List<Supplier<ObjectNode>>> allValues = captor.getAllValues();
            List<Supplier<ObjectNode>> sortedSettings = allValues.get(0);

            // SERVICE (1) < ACCOUNT (2). Sorted order should be SERVICE then ACCOUNT.
            assertEquals(2, sortedSettings.size());
            assertEquals(serviceSetting, sortedSettings.get(0));
            assertEquals(accountSetting, sortedSettings.get(1));
        }

        @Test
        @DisplayName("Should notify shared cache synchronizer with correct hit data")
        void executeVerifiesCacheHitNotification() {
            Input input = createInput(ConfigurationType.ACCOUNT, null);
            SettingsAccount setting = createSettingsAccount(ConfigurationType.ACCOUNT, objectMapper.createObjectNode());

            when(settingsAccountRepository.findAll(ACCOUNT_ID, SERVICE_NAME)).thenReturn(List.of(setting));
            Map<String, Object> mergedMap = Map.of("k", "v");
            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(mergedMap);

            victim.execute(input);

            ArgumentCaptor<ConfigurationHitEvent> captor = ArgumentCaptor.forClass(ConfigurationHitEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            ResolvedConfiguration resolvedConfiguration = captor.getValue().resolvedConfiguration();
            assertEquals(ACCOUNT_ID, resolvedConfiguration.accountId());
            assertEquals(SERVICE_NAME, resolvedConfiguration.serviceName());
            assertEquals(ConfigurationType.ACCOUNT, resolvedConfiguration.configurationType());
            assertEquals(List.of(setting), resolvedConfiguration.settingsAccounts());
            assertEquals(mergedMap, resolvedConfiguration.mergedSettings());
        }
    }

    private Input createInput(ConfigurationType type, String privateKey) {
        return new Input(ACCOUNT_ID, SERVICE_NAME, type, privateKey);
    }

    private SettingsAccount createSettingsAccount(ConfigurationType type, ObjectNode content) {
        return new SettingsAccount(type, ACCOUNT_ID, SERVICE_NAME, content);
    }
}
