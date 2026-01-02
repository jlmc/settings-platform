package io.github.jlmc.settings.service.domain.entities;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigurationTypeTest {

    @Test
    void all_the_values_of_the_enum_should_be_in_the_pattern() {
        String expectedPattern = Arrays.stream(ConfigurationType.values()).map(Object::toString).collect(Collectors.joining("|", "^(", ")$"));
        assertEquals(ConfigurationType.PATTERN, expectedPattern);
    }

    @Test
    void fromString() {
        assertEquals(ConfigurationType.ACCOUNT, ConfigurationType.fromString("ACCOUNT"));
        assertEquals(ConfigurationType.ACCOUNT, ConfigurationType.fromString("account"));
        assertEquals(ConfigurationType.ACCOUNT, ConfigurationType.fromString("Account"));
    }
}
