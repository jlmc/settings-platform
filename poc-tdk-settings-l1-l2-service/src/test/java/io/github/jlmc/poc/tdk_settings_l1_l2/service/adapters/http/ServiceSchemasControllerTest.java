package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.JsonSchemaRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.ServiceJsonSchemasRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.mappers.ServiceJsonSchemasRepresentationMapper;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.JsonSchema;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.Rsa;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ServiceJsonSchemas;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.exceptions.NotFoundException;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.DeleteSchemaUseCase;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.GetServiceSchemasUseCase;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.PersistSchemaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ServiceSchemasControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersistSchemaUseCase persistSchemaUseCase;

    @MockitoBean
    private DeleteSchemaUseCase deleteSchemaUseCase;

    @MockitoBean
    private GetServiceSchemasUseCase getServiceSchemasUseCase;

    @MockitoBean
    private ServiceJsonSchemasRepresentationMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .build();
    }

    @Test
    void defineSchema() throws Exception {
        ObjectNode schemaContent = objectMapper.createObjectNode().put("type", "object");
        JsonSchemaRepresentation schemaRep = new JsonSchemaRepresentation("ACCOUNT", schemaContent);
        ServiceJsonSchemasRepresentation payload = new ServiceJsonSchemasRepresentation("my-service", List.of(schemaRep), new Rsa("key"));

        JsonSchema jsonSchema = new JsonSchema("ACCOUNT", schemaContent);
        ServiceJsonSchemas entity = new ServiceJsonSchemas("my-service", List.of(jsonSchema), new Rsa("key"));

        when(mapper.toEntity(any(ServiceJsonSchemasRepresentation.class))).thenReturn(entity);
        when(persistSchemaUseCase.execute(any())).thenReturn(entity);
        when(mapper.toRepresentation(any(ServiceJsonSchemas.class))).thenReturn(payload);

        mockMvc.perform(put("/schemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service_name").value("my-service"));

        verify(persistSchemaUseCase).execute(entity);
    }

    @Test
    void getServiceSchemas() throws Exception {
        ObjectNode schemaContent = objectMapper.createObjectNode().put("type", "object");
        JsonSchemaRepresentation schemaRep = new JsonSchemaRepresentation("ACCOUNT", schemaContent);
        ServiceJsonSchemasRepresentation payload = new ServiceJsonSchemasRepresentation("my-service", List.of(schemaRep), new Rsa("key"));

        JsonSchema jsonSchema = new JsonSchema("ACCOUNT", schemaContent);
        ServiceJsonSchemas entity = new ServiceJsonSchemas("my-service", List.of(jsonSchema), new Rsa("key"));

        when(getServiceSchemasUseCase.execute("my-service")).thenReturn(entity);
        when(mapper.toRepresentation(entity)).thenReturn(payload);

        mockMvc.perform(get("/schemas/my-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service_name").value("my-service"));
    }

    @Test
    void getServiceSchemasNotFound() throws Exception {
        when(getServiceSchemasUseCase.execute("unknown")).thenThrow(new NotFoundException("Service unknown not found"));

        mockMvc.perform(get("/schemas/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Service unknown not found"));
    }

    @Test
    void deleteServiceSchemas() throws Exception {
        doNothing().when(deleteSchemaUseCase).execute("my-service");

        mockMvc.perform(delete("/schemas/my-service"))
                .andExpect(status().isNoContent());

        verify(deleteSchemaUseCase).execute("my-service");
    }

    @Test
    void defineSchemaValidationError() throws Exception {
        ServiceJsonSchemasRepresentation payload = new ServiceJsonSchemasRepresentation("", List.of(), null);

        mockMvc.perform(put("/schemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());
    }
}
