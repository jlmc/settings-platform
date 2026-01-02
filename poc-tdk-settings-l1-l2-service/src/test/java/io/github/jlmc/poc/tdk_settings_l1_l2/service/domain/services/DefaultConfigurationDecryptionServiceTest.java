package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.services;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ResolvedConfiguration;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.inputs.ResolveConfigurationInput;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.ObjectNodeMerger;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SettingsAccountDecrypter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultConfigurationDecryptionServiceTest {

    @Mock
    private SettingsAccountDecrypter settingsAccountDecrypter;
    @Mock
    private ObjectNodeMerger objectNodeMerger;

    @InjectMocks
    private DefaultConfigurationDecryptionService sut;

    @Test
    @DisplayName("Should return merged settings directly when no private key is provided")
    void returnMergedSettingsWhenNoPrivateKey() {
        ResolveConfigurationInput input = new ResolveConfigurationInput("acc", "srv", ConfigurationType.USER, null);
        Map<String, Object> mergedMap = Map.of("k", "v");
        ResolvedConfiguration resolved = new ResolvedConfiguration("acc", "srv", ConfigurationType.USER, null, List.of(), mergedMap);

        Map<String, Object> result = sut.decryptForReturn(input, resolved);

        assertEquals(mergedMap, result);
        verifyNoInteractions(settingsAccountDecrypter, objectNodeMerger);
    }

    @Test
    @DisplayName("Should return merged settings when schemas are null")
    void returnMergedSettingsWhenSchemasAreNull() {
        ResolveConfigurationInput input = new ResolveConfigurationInput("acc", "srv", ConfigurationType.USER, "private-key");
        SettingsAccount accountSettings = new SettingsAccount(ConfigurationType.ACCOUNT, "acc", "srv", JsonNodeFactory.instance.objectNode());
        ResolvedConfiguration resolved = new ResolvedConfiguration("acc", "srv", ConfigurationType.USER, null, List.of(accountSettings), Map.of());
        Map<String, Object> mergedMap = Map.of("k", "v");

        when(objectNodeMerger.mergeContentsAsMap(any())).thenReturn(mergedMap);

        Map<String, Object> result = sut.decryptForReturn(input, resolved);

        assertEquals(mergedMap, result);
        verifyNoInteractions(settingsAccountDecrypter);
        verify(objectNodeMerger).mergeContentsAsMap(any());
    }

    @Test
    @DisplayName("Should decrypt and merge settings when private key and schemas are present")
    void decryptAndMergeSettings() {
        ResolveConfigurationInput input = new ResolveConfigurationInput("acc", "srv", ConfigurationType.USER, "private-key");
        ServiceJsonSchemas schemas = mock(ServiceJsonSchemas.class);
        ObjectNode content = JsonNodeFactory.instance.objectNode().put("encrypted", "value");
        SettingsAccount originalAccount = new SettingsAccount(ConfigurationType.ACCOUNT, "acc", "srv", content);
        List<SettingsAccount> originalSettings = List.of(originalAccount);
        ResolvedConfiguration resolved = new ResolvedConfiguration("acc", "srv", ConfigurationType.USER, schemas, originalSettings, Map.of());
        
        ObjectNode decryptedContent = JsonNodeFactory.instance.objectNode().put("decrypted", "value");
        SettingsAccount decryptedAccount = originalAccount.copyWithJson(decryptedContent);
        List<SettingsAccount> decryptedSettings = List.of(decryptedAccount);
        
        Map<String, Object> mergedMap = Map.of("decrypted", "value");

        when(settingsAccountDecrypter.decryptConfigurationJsons(anyList(), eq(schemas), eq("private-key")))
                .thenReturn(decryptedSettings);
        when(objectNodeMerger.mergeContentsAsMap(anyList())).thenReturn(mergedMap);

        Map<String, Object> result = sut.decryptForReturn(input, resolved);

        assertEquals(mergedMap, result);
        
        ArgumentCaptor<List<SettingsAccount>> listCaptor = ArgumentCaptor.forClass(List.class);
        verify(settingsAccountDecrypter).decryptConfigurationJsons(listCaptor.capture(), eq(schemas), eq("private-key"));
        
        List<SettingsAccount> capturedList = listCaptor.getValue();
        assertEquals(1, capturedList.size());
        // Verify it is a copy (different object but same content before decryption)
        assertNotSame(originalAccount, capturedList.get(0));
        assertEquals(originalAccount.content(), capturedList.get(0).content());
        
        verify(objectNodeMerger).mergeContentsAsMap(anyList());
    }
}
