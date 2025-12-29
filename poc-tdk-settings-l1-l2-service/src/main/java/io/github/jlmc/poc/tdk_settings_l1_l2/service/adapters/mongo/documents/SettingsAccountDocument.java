package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import tools.jackson.databind.node.ObjectNode;

@Document("settingsAccounts")
@CompoundIndex(
        name = "account_type_service_idx",
        // These must match the @Field(name = "...") values
        def = "{'account_id': 1, 'type': 1, 'service_name': 1}",
        unique = true
)
public class SettingsAccountDocument {
    @Id
    private final String id;

    @NotNull
    @Field(name = "type")
    private final @NotNull ConfigurationType type;

    @NotBlank
    @Field(name = "account_id")
    private final String accountId;

    @NotBlank
    @Field(name = "service_name")
    private final String serviceName;

    @Version
    @Field(name = "version")

    private Long version;

    @NotNull
    @Field(name = "content")
    private ObjectNode content;

    public SettingsAccountDocument(
            String id,
            @NotNull
            ConfigurationType type,
            @NotBlank
            String accountId,
            @NotBlank
            String serviceName,
            Long version,
            @NotNull
            ObjectNode content
    ) {
        this.id = id;
        this.type = type;
        this.accountId = accountId;
        this.serviceName = serviceName;
        this.version = version;
        this.content = content;
    }

    public static SettingsAccountDocument from(SettingsAccount entity) {
        return new SettingsAccountDocument(
                null,
                entity.type(),
                entity.accountId(),
                entity.serviceName(),
                null,
                entity.schemaContent()
        );
    }

    public SettingsAccount toEntity() {
        return new SettingsAccount(
                this.type,
                this.accountId,
                this.serviceName,
                this.content
        );

    }

    public void updateFromEntity(SettingsAccount entity) {
        this.content = entity.schemaContent();
    }

    @Id
    public String id() {
        return id;
    }

    @Field(name = "type")
    public @NotNull ConfigurationType type() {
        return type;
    }

    @Field(name = "account_id")
    public String accountId() {
        return accountId;
    }

    @Field(name = "service_name")
    public String serviceName() {
        return serviceName;
    }

    @Version
    public Long version() {
        return version;
    }

    @Field(name = "content")
    public ObjectNode content() {
        return content;
    }

    @Override
    public String toString() {
        return "SettingsAccountDocument[id=" + id + ", " +
                "type=" + type + ", " +
                "accountId=" + accountId + ", " +
                "serviceName=" + serviceName + ']';
    }

}
