package io.github.jlmc.settings.service.adapters.json;

import io.github.jlmc.settings.service.domain.ports.ObjectNodeMerger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class JsonOperationsConfiguration {

    @Bean
    ObjectNodeMerger ObjectNodeMerger(ObjectMapper objectMapper) {
        return new JacksonObjectNodeMerger(objectMapper);
    }

}
