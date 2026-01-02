package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.ServiceJsonSchemasRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.mappers.ServiceJsonSchemasRepresentationMapper;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.schemas.DeleteSchemaUseCase;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.schemas.GetServiceSchemasUseCase;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.schemas.PersistSchemaUseCase;
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

/**
 * Controller for managing service JSON schemas.
 */
@RestController
@RequestMapping(
    path = "/schemas"
)
public class ServiceSchemasController {

    private final PersistSchemaUseCase persistSchemaUseCase;
    private final DeleteSchemaUseCase deleteSchemaUseCase;
    private final GetServiceSchemasUseCase getServiceSchemasUseCase;
    private final ServiceJsonSchemasRepresentationMapper mapper;

    public ServiceSchemasController(PersistSchemaUseCase persistSchemaUseCase,
                                    DeleteSchemaUseCase deleteSchemaUseCase,
                                    GetServiceSchemasUseCase getServiceSchemasUseCase,
                                    ServiceJsonSchemasRepresentationMapper mapper) {
        this.persistSchemaUseCase = persistSchemaUseCase;
        this.deleteSchemaUseCase = deleteSchemaUseCase;
        this.getServiceSchemasUseCase = getServiceSchemasUseCase;
        this.mapper = mapper;
    }

    /**
     * Defines or updates the JSON schemas for a service.
     *
     * <p>Example call using curl:
     * <pre>{@code
     * curl -X PUT http://localhost:8080/schemas \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "serviceName": "my-service",
     *     "jsonSchemas": [
     *       {
     *         "type": "ACCOUNT",
     *         "value": {
     *           "type": "object",
     *           "properties": {
     *             "setting1": { "type": "string" }
     *           }
     *         }
     *       }
     *     ],
     *     "rsa": {
     *       "publicKey": "ssh-rsa AAAAB3Nza..."
     *     }
     *   }'
     * }</pre>
     *
     * @param payload the schemas to define
     * @return the defined schemas
     */
    @PutMapping
    public ServiceJsonSchemasRepresentation defineSchema(@RequestBody @Validated ServiceJsonSchemasRepresentation payload) {
        ServiceJsonSchemas entity = persistSchemaUseCase.execute(mapper.toEntity(payload));
        return mapper.toRepresentation(entity);
    }

    /**
     * Retrieves the JSON schemas for a specified service.
     *
     * <p>Example call using curl:
     * <pre>{@code
     * curl -X GET http://localhost:8080/schemas/my-service
     * }</pre>
     *
     * @param serviceName the name of the service
     * @return the service schemas
     */
    @GetMapping(path = "/{serviceName}")
    public ServiceJsonSchemasRepresentation getServiceSchemas(@PathVariable String serviceName) {
        ServiceJsonSchemas entity = getServiceSchemasUseCase.execute(serviceName);
        return mapper.toRepresentation(entity);
    }

    /**
     * Deletes the JSON schemas for a specified service.
     *
     * <p>Example call using curl:
     * <pre>{@code
     * curl -X DELETE http://localhost:8080/schemas/my-service
     * }</pre>
     *
     * @param serviceName the name of the service to delete schemas for
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping(path = "/{serviceName}")
    public void deleteServiceSchemas(@PathVariable String serviceName) {
        deleteSchemaUseCase.execute(serviceName.toLowerCase());
    }

}
