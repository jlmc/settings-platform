package io.github.jlmc.poc.settings.sdk.domain.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SettingsAccount(
        ConfigurationType type,
        String accountId,
        String serviceName,
        ObjectNode content
) implements Supplier<ObjectNode> {

    public int priority() {
        return this.type.priority();
    }

    public static Predicate<SettingsAccount> settingsAccountWithinPriorityThreshold(ConfigurationType type) {
        return it -> it.priority() <= type.priority();
    }

    public static final Comparator<SettingsAccount> BY_PRIORITY = Comparator.comparing(SettingsAccount::priority);

    @Override
    public ObjectNode get() {
        return this.content;
    }

    public SettingsAccount copyWithJson(ObjectNode contentValue) {
        return new SettingsAccount(
                this.type,
                this.accountId,
                this.serviceName,
                contentValue
        );
    }

    public SettingsAccount copy() {
        return new SettingsAccount(
                this.type,
                this.accountId,
                this.serviceName,
                this.content.deepCopy()
        );
    }

    public static List<Supplier<ObjectNode>> asSuppliers(List<SettingsAccount> settings) {
        return settings.stream().map(account -> (Supplier<ObjectNode>) account).toList();
    }
}
