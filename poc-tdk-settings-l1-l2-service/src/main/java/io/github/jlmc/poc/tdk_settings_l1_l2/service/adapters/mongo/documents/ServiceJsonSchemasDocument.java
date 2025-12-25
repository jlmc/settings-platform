package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.mongo.documents;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.Rsa;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("configurationSchemas")
public record ServiceJsonSchemasDocument(
        @Id @Indexed(unique = true, name = "CONFIGURATIONS_SCHEMA_UNIQUE_SERVICE_NAME_INDEX")
        @NotBlank String serviceName,
        @NotEmpty List<@Valid JsonSchema> schemas,
        Rsa rsa
) {
    public ServiceJsonSchemasDocument {
        if (serviceName == null || serviceName.isBlank()) {
            throw new IllegalArgumentException("serviceName cannot be null or blank");
        }
        serviceName = serviceName.trim().toLowerCase();

        schemas = schemas != null ? List.copyOf(schemas) : List.of();
    }

    public static ServiceJsonSchemasDocument from(ServiceJsonSchemas entity) {
        return new ServiceJsonSchemasDocument(
                entity.serviceName().toLowerCase(),
                entity.schemas(),
                entity.rsa()
        );
    }

    public ServiceJsonSchemas toEntity() {
        return new ServiceJsonSchemas(
                this.serviceName,
                this.schemas,
                this.rsa
        );
    }
}
