package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.mappers;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.JsonSchemaRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.ServiceJsonSchemasRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceJsonSchemasRepresentationMapper {

    @Mapping(target = "jsonSchemas", source = "schemas")
    ServiceJsonSchemasRepresentation toRepresentation(ServiceJsonSchemas entity);

    @Mapping(target = "type", source = "schemaType")
    JsonSchemaRepresentation toRepresentation(JsonSchema entity);

    @Mapping(target = "schemas", source = "jsonSchemas")
    ServiceJsonSchemas toEntity(ServiceJsonSchemasRepresentation representation);

    @Mapping(target = "schemaType", source = "type")
    JsonSchema toEntity(JsonSchemaRepresentation representation);
}
