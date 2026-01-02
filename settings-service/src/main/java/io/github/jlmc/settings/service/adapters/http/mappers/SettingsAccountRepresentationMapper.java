package io.github.jlmc.settings.service.adapters.http.mappers;

import io.github.jlmc.settings.service.adapters.http.data.SettingsAccountRepresentation;
import io.github.jlmc.settings.service.domain.entities.SettingsAccount;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SettingsAccountRepresentationMapper {
    SettingsAccountRepresentation toRepresentation(SettingsAccount entity);
}
