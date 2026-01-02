package io.github.jlmc.poc.commons.settings.redis;

public interface RedisSettingsProvider extends AutoCloseable {
    /**
     * Retrieves the value associated with the given key.
     * * @param key the setting key (the namespace prefix is managed internally).
     * @return the value or null if the key does not exist or the provider is unavailable.
     */
    String getValue(String key);

    /**
     * Returns the Redis namespace (prefix) used by this provider.
     */
    String getNamespace();

    /**
     * Checks if the connection to the provider is active and functional.
     */
    boolean isAvailable();

    @Override
    void close();
}
