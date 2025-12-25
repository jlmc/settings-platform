package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.ServiceJsonSchemas;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(
    path = "/schemas",
    produces = "application/json",
    consumes = "application/json"
)
public class ServiceSchemasController {

    @PutMapping
    public ServiceJsonSchemas defineSchema(@RequestBody @Validated ServiceJsonSchemas payload) {
        return payload;
    }

    @GetMapping(path = "/{serviceName}")
    public ServiceJsonSchemas getServiceSchemas(@PathVariable String serviceName) {
        return new ServiceJsonSchemas(
            serviceName,
            List.of(
                new JsonSchema(
                    "ACCOUNT",
                    """
                    {
                      "type": "object",
                      "properties": {
                        "accountSetting1": { "type": "boolean" },
                        "accountSetting2": { "type": "string" }
                      },
                      "required": ["accountSetting1"]
                    }
                    """
                )

            ),
            null
        );
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{serviceName}")
    public void deleteServiceSchemas(@PathVariable String serviceName) {
        // Logic to delete the schemas for the specified service
    }


}


/*
@Validated
@RestController
@RequestMapping(
    path = ["/industries-settings/configuration-schemas"],
    produces = [MediaType.APPLICATION_JSON_VALUE]
)
class ConfigurationSchemaController(
    private val configurationService: ConfigurationSchemaService,
) {

    @PutMapping
    fun defineConfigurationSchema(
        @Validated @RequestBody configurationSchemaDto: ConfigurationSchemaDto,
    ): ConfigurationSchemaDto {
        return configurationService.defineConfigurationSchema(configurationSchemaDto)
    }

    @AuditInfo(operation = AuditEventOperation.INDUSTRIES_SETTINGS_READ_CONFIGURATION_SCHEMA)
    @RequireScopes(RequestScopes.INDUSTRIES_SETTINGS_CONFIGURATION_SCHEMAS_READ)
    @GetMapping("/{service}")
    fun getConfigurationSchema(
        @PathVariable(required = true) @NotBlank service: String,
    ): ConfigurationSchemaDto {
        return configurationService.getConfigurationSchemaByService(service)
    }

    @AuditInfo(operation = AuditEventOperation.INDUSTRIES_SETTINGS_READ_CONFIGURATION_SCHEMA)
    @RequireScopes(RequestScopes.INDUSTRIES_SETTINGS_CONFIGURATION_SCHEMAS_READ)
    @GetMapping("/{service}/schemas/{type}")
    fun getConfigurationSchemaType(
        @PathVariable(required = true) @NotBlank service: String,
        @PathVariable(required = true) @Pattern(regexp = "SERVICE|ACCOUNT|RING_GROUP|AGENT|CLIENT") type: String,
    ): JsonContent {
        return configurationService.getConfigurationSchemaContentByServiceAndType(service, type)
    }
}
 */