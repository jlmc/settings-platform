package io.github.jlmc.settings.service.adapters.http;

import io.github.jlmc.settings.service.domain.exceptions.NotFoundException;
import io.github.jlmc.settings.service.domain.inputs.ResolveConfigurationInput;
import io.github.jlmc.settings.service.domain.usercases.configurations.GetConfigurationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.Map;

import static io.github.jlmc.settings.service.domain.entities.ConfigurationType.ACCOUNT;
import static io.github.jlmc.settings.service.domain.entities.ConfigurationType.AGENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {ConfigurationsAccountController.class})
public class ConfigurationsAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetConfigurationUseCase getConfigurationUseCase;

    @Test
    void when_configurations_exists_it_returns_200() throws Exception {
        ResolveConfigurationInput expectedInput = new ResolveConfigurationInput("account-123", "service-abc", ACCOUNT, null);
        when(getConfigurationUseCase.execute(expectedInput)).thenReturn(
                Map.of("key1", "value1", "key2", 1234)
        );

        mockMvc.perform(get("/configurations/{account_id}/{service_name}/{type}",
                        "account-123",
                        "service-abc",
                        "ACCOUNT")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                        {"key1":"value1", "key2":1234}
                                        """,
                                JsonCompareMode.STRICT)
                );

    }

    @Test
    void when_configurations_exists_with_private_key_it_returns_200() throws Exception {
        ResolveConfigurationInput expectedInput = new ResolveConfigurationInput("account-123", "service-abc", ACCOUNT, "my-private-key");
        when(getConfigurationUseCase.execute(expectedInput)).thenReturn(
                Map.of("key1", "decrypted-value")
        );

        mockMvc.perform(get("/configurations/{account_id}/{service_name}/{type}",
                        "account-123",
                        "service-abc",
                        "ACCOUNT")
                        .header("X-Private-Key", "my-private-key")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isOk(),
                        content().contentType(MediaType.APPLICATION_JSON),
                        content().json("""
                                        {"key1":"decrypted-value"}
                                        """,
                                JsonCompareMode.STRICT)
                );
    }

    @Test
    void when_requesting_different_type_it_passes_correct_type_to_use_case() throws Exception {
        ResolveConfigurationInput expectedInput = new ResolveConfigurationInput("account-123", "service-abc", AGENT, null);
        when(getConfigurationUseCase.execute(expectedInput)).thenReturn(Map.of("type", "AGENT"));

        mockMvc.perform(get("/configurations/{account_id}/{service_name}/{type}",
                        "account-123",
                        "service-abc",
                        "AGENT")
                )
                .andExpect(status().isOk())
                .andExpect(content().json("{\"type\":\"AGENT\"}"));
    }

    @Test
    void when_configurations_not_exists_it_returns_404() throws Exception {
        when(getConfigurationUseCase.execute(any(ResolveConfigurationInput.class)))
                .thenThrow(new NotFoundException("Configuration not found"));

        mockMvc.perform(get("/configurations/{account_id}/{service_name}/{type}",
                        "account-123",
                        "service-abc",
                        "ACCOUNT")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE),
                        content().json("""
                                        {
                                          "detail": "Configuration not found",
                                          "instance": "/configurations/account-123/service-abc/ACCOUNT",
                                          "status": 404,
                                          "title": "Not Found"
                                        }
                                        """,
                                JsonCompareMode.STRICT)
                );

    }


}
