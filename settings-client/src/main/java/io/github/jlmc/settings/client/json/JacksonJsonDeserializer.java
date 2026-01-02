package io.github.jlmc.settings.client.json;


import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.settings.client.exceptions.DeserializationSettingsClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of JsonDeserializer using the Jackson library.
 * Encapsulates Jackson dependency logic.
 */
public class JacksonJsonDeserializer implements JsonDeserializer {

    private static final Logger logger = LoggerFactory.getLogger(JacksonJsonDeserializer.class);

    private final ObjectMapper objectMapper;

    public JacksonJsonDeserializer() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public JacksonJsonDeserializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T deserialize(String source, Class<T> type) throws DeserializationSettingsClientException {
        try {
            return objectMapper.readValue(source, type);
        } catch (JacksonException e) {
            logger.error("Failed to map source to {} object. source: {}", type.getSimpleName(), source, e);
            throw new DeserializationSettingsClientException("JSON deserialization error. " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T readValueAs(Object source, Class<T> type) throws DeserializationSettingsClientException {
        try {
            String json = objectMapper.writeValueAsString(source);
            return objectMapper.readValue(json, type);
        } catch (JacksonException e) {
            logger.error("Failed to map source to {} object. source: {}", type.getSimpleName(), source, e);
            throw new DeserializationSettingsClientException("JSON deserialization error. " + e.getMessage(), e);
        }
    }
}
