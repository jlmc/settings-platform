package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.events.ConfigurationHitEvent;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ObjectNodeMerger;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ServiceJsonSchemasRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountRepository;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.configurations.Input;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.node.JsonNodeFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultConfigurationResolverTest {

    @Mock
    private SettingsAccountRepository settingsAccountRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private ObjectNodeMerger objectNodeMerger;
    @Mock
    private ServiceJsonSchemasRepository serviceJsonSchemasRepository;

    @InjectMocks
    private DefaultConfigurationResolver sut;

    private final String accountId = "account-1";
    private final String serviceName = "service-a";
    private final ConfigurationType type = ConfigurationType.USER;

    @Nested
    @DisplayName("Resolve Method Tests")
    class ResolveTests {

        @Test
        @DisplayName("Should resolve configuration successfully when settings and schemas exist")
        void resolveSuccessfully() {
            Input input = new Input(accountId, serviceName, type, null);
            SettingsAccount accountSettings = new SettingsAccount(ConfigurationType.ACCOUNT, accountId, serviceName, JsonNodeFactory.instance.objectNode());
            SettingsAccount userSettings = new SettingsAccount(ConfigurationType.USER, accountId, serviceName, JsonNodeFactory.instance.objectNode());
            List<SettingsAccount> allSettings = List.of(accountSettings, userSettings);
            ServiceJsonSchemas schemas = mock(ServiceJsonSchemas.class);
            Map<String, Object> mergedMap = Map.of("key", "value");

            when(settingsAccountRepository.findAll(accountId, serviceName)).thenReturn(allSettings);
            when(serviceJsonSchemasRepository.findByServiceName(serviceName)).thenReturn(Optional.of(schemas));
            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(mergedMap);

            ResolvedConfiguration result = sut.resolve(input);

            assertNotNull(result);
            assertEquals(accountId, result.accountId());
            assertEquals(serviceName, result.serviceName());
            assertEquals(type, result.configurationType());
            assertEquals(schemas, result.serviceJsonSchemas());
            assertEquals(mergedMap, result.mergedSettings());
            assertEquals(2, result.settingsAccounts().size());

            verify(eventPublisher).publishEvent(any(ConfigurationHitEvent.class));
        }

        @Test
        @DisplayName("Should resolve configuration without schemas when they are not found")
        void resolveWithoutSchemas() {
            Input input = new Input(accountId, serviceName, type, null);
            SettingsAccount settings = new SettingsAccount(ConfigurationType.ACCOUNT, accountId, serviceName, JsonNodeFactory.instance.objectNode());
            
            when(settingsAccountRepository.findAll(accountId, serviceName)).thenReturn(List.of(settings));
            when(serviceJsonSchemasRepository.findByServiceName(serviceName)).thenReturn(Optional.empty());
            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(Collections.emptyMap());

            ResolvedConfiguration result = sut.resolve(input);

            assertNull(result.serviceJsonSchemas());
            verify(eventPublisher).publishEvent(any(ConfigurationHitEvent.class));
        }

        @Test
        @DisplayName("Should throw NotFoundException when no settings are found")
        void throwNotFoundExceptionWhenNoSettings() {
            Input input = new Input(accountId, serviceName, type, null);
            when(settingsAccountRepository.findAll(accountId, serviceName)).thenReturn(Collections.emptyList());

            assertThrows(NotFoundException.class, () -> sut.resolve(input));
            verifyNoInteractions(eventPublisher, objectNodeMerger, serviceJsonSchemasRepository);
        }

        @Test
        @DisplayName("Should throw NotFoundException when no settings match priority threshold")
        void throwNotFoundExceptionWhenNoSettingsMatchPriority() {
            Input input = new Input(accountId, serviceName, ConfigurationType.SERVICE, null);
            SettingsAccount accountSettings = new SettingsAccount(ConfigurationType.ACCOUNT, accountId, serviceName, JsonNodeFactory.instance.objectNode());
            
            when(settingsAccountRepository.findAll(accountId, serviceName)).thenReturn(List.of(accountSettings));

            assertThrows(NotFoundException.class, () -> sut.resolve(input));
        }

        @Test
        @DisplayName("Should filter and sort settings by priority")
        void filterAndSortSettings() {
            Input input = new Input(accountId, serviceName, ConfigurationType.AGENT, null);
            SettingsAccount serviceSettings = new SettingsAccount(ConfigurationType.SERVICE, accountId, serviceName, JsonNodeFactory.instance.objectNode().put("p", 1));
            SettingsAccount userSettings = new SettingsAccount(ConfigurationType.USER, accountId, serviceName, JsonNodeFactory.instance.objectNode().put("p", 4));
            SettingsAccount agentSettings = new SettingsAccount(ConfigurationType.AGENT, accountId, serviceName, JsonNodeFactory.instance.objectNode().put("p", 3));
            SettingsAccount accountSettings = new SettingsAccount(ConfigurationType.ACCOUNT, accountId, serviceName, JsonNodeFactory.instance.objectNode().put("p", 2));

            // Unordered list
            when(settingsAccountRepository.findAll(accountId, serviceName))
                    .thenReturn(List.of(userSettings, serviceSettings, agentSettings, accountSettings));
            when(serviceJsonSchemasRepository.findByServiceName(serviceName)).thenReturn(Optional.empty());
            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(Collections.emptyMap());

            ResolvedConfiguration result = sut.resolve(input);

            // Should only have SERVICE(1), ACCOUNT(2), AGENT(3) because input is AGENT(3). USER(4) is filtered out.
            // Should be sorted by priority: 1, 2, 3
            assertEquals(3, result.settingsAccounts().size());
            assertEquals(ConfigurationType.SERVICE, result.settingsAccounts().get(0).type());
            assertEquals(ConfigurationType.ACCOUNT, result.settingsAccounts().get(1).type());
            assertEquals(ConfigurationType.AGENT, result.settingsAccounts().get(2).type());
        }

        @Test
        @DisplayName("Should publish ConfigurationHitEvent with correct data")
        void publishCorrectEvent() {
            Input input = new Input(accountId, serviceName, type, null);
            SettingsAccount settings = new SettingsAccount(ConfigurationType.SERVICE, accountId, serviceName, JsonNodeFactory.instance.objectNode());
            
            when(settingsAccountRepository.findAll(accountId, serviceName)).thenReturn(List.of(settings));
            when(serviceJsonSchemasRepository.findByServiceName(serviceName)).thenReturn(Optional.empty());
            when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(Map.of("k", "v"));

            sut.resolve(input);

            ArgumentCaptor<ConfigurationHitEvent> eventCaptor = ArgumentCaptor.forClass(ConfigurationHitEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            
            ConfigurationHitEvent event = eventCaptor.getValue();
            assertNotNull(event.resolvedConfiguration());
            assertEquals(accountId, event.resolvedConfiguration().accountId());
        }
    }
}
