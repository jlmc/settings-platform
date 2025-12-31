package io.gihub.jlmc.poc.commons.settings;

public enum ConfigurationType {
    SERVICE(1),
    ACCOUNT(2),
    AGENT(3),
    USER(4);

    private final int priority;

    ConfigurationType(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    public static ConfigurationType fromString(String value) {
        if (value == null) {
            return null;
        }
        return ConfigurationType.valueOf(value.toUpperCase());
    }

    public static final String PATTERN = "^(SERVICE|ACCOUNT|AGENT|USER)$";
}
