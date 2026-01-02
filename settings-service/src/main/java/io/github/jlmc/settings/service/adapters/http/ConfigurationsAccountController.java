package io.github.jlmc.settings.service.adapters.http;

import io.github.jlmc.settings.service.domain.entities.ConfigurationType;
import io.github.jlmc.settings.service.domain.inputs.ResolveConfigurationInput;
import io.github.jlmc.settings.service.domain.usercases.configurations.GetConfigurationUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/configurations/{account_id}/{service_name}/{type}")
public class ConfigurationsAccountController {

    private final GetConfigurationUseCase getConfigurationUseCase;

    public ConfigurationsAccountController(GetConfigurationUseCase getConfigurationUseCase) {
        this.getConfigurationUseCase = getConfigurationUseCase;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfiguration(
            @PathVariable("account_id") String accountId,
            @PathVariable("service_name") String serviceName,
            @PathVariable String type,
            @RequestHeader(value = "X-Private-Key", required = false) String privateKey
    ) {

        Map<String, Object> result = getConfigurationUseCase.execute(
                new ResolveConfigurationInput(
                        accountId,
                        serviceName,
                        ConfigurationType.fromString(type),
                        privateKey
                )
        );

        return ResponseEntity.ok(result);

    }
}
