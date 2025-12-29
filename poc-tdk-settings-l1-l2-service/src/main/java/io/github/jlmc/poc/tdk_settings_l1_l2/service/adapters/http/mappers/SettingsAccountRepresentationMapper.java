package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.mappers;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.SettingsAccountRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SettingsAccountRepresentationMapper {
    SettingsAccountRepresentation toRepresentation(SettingsAccount entity);
}
