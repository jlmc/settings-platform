package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.data.SettingsAccountRepresentation;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.http.mappers.SettingsAccountRepresentationMapper;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.ConfigurationType;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.entities.SettingsAccount;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings.DeleteSettingsAccountUseCase;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings.GetSettingsAccountUseCase;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings.Input;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.usercases.settings.SaveSettingsAccountUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SettingsAccountControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SaveSettingsAccountUseCase saveSettingsAccountUseCase;

    @MockitoBean
    private DeleteSettingsAccountUseCase deleteSettingsAccountUseCase;

    @MockitoBean
    private GetSettingsAccountUseCase getSettingsAccountUseCase;

    @MockitoBean
    private SettingsAccountRepresentationMapper mapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void putSettings() throws Exception {
        ObjectNode payload = objectMapper.createObjectNode().put("foo", "bar");
        SettingsAccount entity = new SettingsAccount(ConfigurationType.ACCOUNT, "acc1", "srv1", payload);
        SettingsAccountRepresentation representation = new SettingsAccountRepresentation("ACCOUNT", "acc1", "srv1", payload);

        when(saveSettingsAccountUseCase.execute(any(SettingsAccount.class))).thenReturn(entity);
        when(mapper.toRepresentation(entity)).thenReturn(representation);

        mockMvc.perform(put("/settings/acc1/srv1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACCOUNT"))
                .andExpect(jsonPath("$.account_id").value("acc1"))
                .andExpect(jsonPath("$.service_name").value("srv1"))
                .andExpect(jsonPath("$.schema_content.foo").value("bar"));

        verify(saveSettingsAccountUseCase).execute(any(SettingsAccount.class));
    }

    @Test
    void deleteSettings() throws Exception {
        mockMvc.perform(delete("/settings/acc1/srv1/account"))
                .andExpect(status().isNoContent());

        verify(deleteSettingsAccountUseCase).execute(new Input("acc1", "srv1", ConfigurationType.ACCOUNT));
    }

    @Test
    void getSettings() throws Exception {
        ObjectNode payload = objectMapper.createObjectNode().put("foo", "bar");
        SettingsAccount entity = new SettingsAccount(ConfigurationType.ACCOUNT, "acc1", "srv1", payload);
        SettingsAccountRepresentation representation = new SettingsAccountRepresentation("ACCOUNT", "acc1", "srv1", payload);

        when(getSettingsAccountUseCase.execute(new Input("acc1", "srv1", ConfigurationType.ACCOUNT)))
                .thenReturn(Optional.of(entity));
        when(mapper.toRepresentation(entity)).thenReturn(representation);

        mockMvc.perform(get("/settings/acc1/srv1/account"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ACCOUNT"))
                .andExpect(jsonPath("$.account_id").value("acc1"))
                .andExpect(jsonPath("$.service_name").value("srv1"))
                .andExpect(jsonPath("$.schema_content.foo").value("bar"));
    }

    @Test
    void getSettingsNotFound() throws Exception {
        when(getSettingsAccountUseCase.execute(any(Input.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/settings/acc1/srv1/account"))
                .andExpect(status().isNotFound());
    }
}
