package io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities;

import tools.jackson.databind.node.ObjectNode;

import java.util.Comparator;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
}
