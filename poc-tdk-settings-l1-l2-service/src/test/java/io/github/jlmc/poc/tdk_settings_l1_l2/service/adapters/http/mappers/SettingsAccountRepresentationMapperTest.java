package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.mappers;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.SettingsAccountRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SettingsAccountRepresentationMapperTest {

    private final SettingsAccountRepresentationMapper sut = new SettingsAccountRepresentationMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void toRepresentation() {
        ObjectNode schemaContent = objectMapper.createObjectNode().put("key", "value");
        SettingsAccount entity = new SettingsAccount(
                ConfigurationType.ACCOUNT,
                "account-123",
                "service-abc",
                schemaContent
        );

        SettingsAccountRepresentation result = sut.toRepresentation(entity);

        assertNotNull(result);
        assertEquals("ACCOUNT", result.type());
        assertEquals("account-123", result.accountId());
        assertEquals("service-abc", result.serviceName());
        assertEquals(schemaContent, result.schemaContent());
    }

    @Test
    void toRepresentationWhenNull() {
        SettingsAccountRepresentation result = sut.toRepresentation(null);

        assertNull(result);
    }
}
