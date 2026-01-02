package io.gihub.jlmc.poc.commons.settings;

import io.gihub.jlmc.poc.commons.settings.auth.AuthCredentials;

import java.util.Objects;

/**
 * Represents a configuration retrieval request sent to the Industries Settings service.
 * <p>
 * This class encapsulates all the required metadata and credentials needed
 * to request configuration data for a specific object (e.g., an agent, account, or team)
 * within a Talkdesk service.
 *
 * @see io.github.jlmc.poc.settings.sdk.domain.entities.ConfigurationType
 * @see AuthCredentials
 */
public record ConfigurationRequest(
        AuthCredentials authCredentials,
        String service,
        io.github.jlmc.poc.settings.sdk.domain.entities.ConfigurationType objectType,
        String objectId,
        String rsaPrivateKey,
        String interactionId,
        String accountId) {

    public ConfigurationRequest(
            AuthCredentials authCredentials,
            String service,
            io.github.jlmc.poc.settings.sdk.domain.entities.ConfigurationType objectType,
            String objectId,
            String rsaPrivateKey,
            String interactionId,
            String accountId
    ) {
        this.authCredentials = Objects.requireNonNull(authCredentials, "authCredentials must not be null");
        if (service == null || service.isBlank()) {
            throw new IllegalArgumentException("service must not be empty or blank.");
        }
        this.service = service;
        this.objectType = Objects.requireNonNull(objectType, "objectType must not be null");
        this.objectId = objectId;
        this.rsaPrivateKey = rsaPrivateKey;
        this.interactionId = interactionId;
        this.accountId = accountId;
    }

    /**
     * Creates a standard configuration request without RSA encryption.
     */
    public static ConfigurationRequest standard(
            AuthCredentials authCredentials,
            String service,
            io.github.jlmc.poc.settings.sdk.domain.entities.ConfigurationType objectType,
            String objectId
    ) {
        return new ConfigurationRequest(authCredentials, service, objectType, objectId, null, null, null);
    }

    public static ConfigurationRequest standardWithAccountId(
            AuthCredentials authCredentials,
            String service,
            io.github.jlmc.poc.settings.sdk.domain.entities.ConfigurationType objectType,
            String objectId,
            String accountId
    ) {
        return new ConfigurationRequest(authCredentials, service, objectType, objectId, null, null, accountId);
    }

    /**
     * Creates a configuration request that includes RSA encryption details.
     */
    public static ConfigurationRequest withRsaEncryption(
            AuthCredentials authCredentials,
            String service,
            io.github.jlmc.poc.settings.sdk.domain.entities.ConfigurationType objectType,
            String objectId,
            String rsaPrivateKey,
            String interactionId
    ) {
        return new ConfigurationRequest(authCredentials, service, objectType, objectId, rsaPrivateKey, interactionId, null);
    }

    @Override
    public String toString() {
        return "ConfigurationRequest(" +
                "service='" + service + '\'' +
                ", objectType=" + objectType +
                ", objectId=" + objectId +
                ", interactionId=" + interactionId +
                ')';
    }
}
