package io.github.jlmc.settings.service.adapters.http;

import io.github.jlmc.settings.service.adapters.http.data.SettingsAccountRepresentation;
import io.github.jlmc.settings.service.adapters.http.mappers.SettingsAccountRepresentationMapper;
import io.github.jlmc.settings.service.domain.entities.SettingsAccount;
import io.github.jlmc.settings.service.domain.exceptions.NotFoundException;
import io.github.jlmc.settings.service.domain.inputs.SettingsInput;
import io.github.jlmc.settings.service.domain.usercases.settings.DeleteSettingsAccountUseCase;
import io.github.jlmc.settings.service.domain.usercases.settings.GetSettingsAccountUseCase;
import io.github.jlmc.settings.service.domain.usercases.settings.SaveSettingsAccountUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

import static io.github.jlmc.settings.service.domain.entities.ConfigurationType.fromString;

@RestController
@RequestMapping("/settings/{account_id}/{service_name}/{type}")
public class SettingsAccountController {

    private final SaveSettingsAccountUseCase saveSettingsAccountUseCase;
    private final DeleteSettingsAccountUseCase deleteSettingsAccountUseCase;
    private final GetSettingsAccountUseCase getSettingsAccountUseCase;
    private final SettingsAccountRepresentationMapper mapper;

    public SettingsAccountController(SaveSettingsAccountUseCase saveSettingsAccountUseCase,
                                     DeleteSettingsAccountUseCase deleteSettingsAccountUseCase,
                                     GetSettingsAccountUseCase getSettingsAccountUseCase,
                                     SettingsAccountRepresentationMapper mapper) {
        this.saveSettingsAccountUseCase = saveSettingsAccountUseCase;
        this.deleteSettingsAccountUseCase = deleteSettingsAccountUseCase;
        this.getSettingsAccountUseCase = getSettingsAccountUseCase;
        this.mapper = mapper;
    }

    @PutMapping
    public ResponseEntity<SettingsAccountRepresentation> putSettings(
            @PathVariable("account_id") String accountId,
            @PathVariable("service_name") String serviceName,
            @PathVariable String type,
            @RequestBody ObjectNode payload) {

        SettingsAccount entity = new SettingsAccount(
                fromString(type.toUpperCase()),
                accountId,
                serviceName,
                payload
        );

        SettingsAccount saved = saveSettingsAccountUseCase.execute(entity);
        return ResponseEntity.ok(mapper.toRepresentation(saved));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteSettings(
            @PathVariable("account_id") String accountId,
            @PathVariable("service_name") String serviceName,
            @PathVariable String type) {

        deleteSettingsAccountUseCase.execute(new SettingsInput(
                accountId,
                serviceName,
                fromString(type.toUpperCase())
        ));
    }

    @GetMapping
    public ResponseEntity<SettingsAccountRepresentation> getSettings(
            @PathVariable("account_id") String accountId,
            @PathVariable("service_name") String serviceName,
            @PathVariable String type) {

        Optional<SettingsAccount> result = getSettingsAccountUseCase.execute(
                new SettingsInput(
                        accountId,
                        serviceName,
                        fromString(type.toUpperCase())
                )
        );

        return result.map(mapper::toRepresentation)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NotFoundException(
                        "SettingsAccount not found for accountId='%s', serviceName='%s', type='%s'".formatted(accountId, serviceName, type)
                ));
    }

}
