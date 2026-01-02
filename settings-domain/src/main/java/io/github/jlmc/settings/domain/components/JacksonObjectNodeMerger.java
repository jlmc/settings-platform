package io.github.jlmc.settings.domain.components;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.jlmc.settings.domain.ports.ObjectNodeMerger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class JacksonObjectNodeMerger implements ObjectNodeMerger {
    private final ObjectMapper objectMapper;

    private static final  TypeReference<Map<String, Object>> MAP_OBJECT_TYPE = new TypeReference<>() {
    };

    public JacksonObjectNodeMerger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> mergeContentsAsMap(List<Supplier<ObjectNode>> sources) {
        ObjectNode mergedNode = mergeContents(sources);
        return objectMapper.convertValue(mergedNode, MAP_OBJECT_TYPE);
    }

    public ObjectNode mergeContents(List<Supplier<ObjectNode>> sources) {
        ObjectNode result = objectMapper.createObjectNode();

        if (sources == null || sources.isEmpty()) {
            return result;
        }

        sources.stream()
                .filter(java.util.Objects::nonNull)
                .map(Supplier::get)
                .filter(java.util.Objects::nonNull)
                .forEach(content -> mergeInto(result, content));

        return result;
    }

    private static void mergeInto(ObjectNode target, ObjectNode source) {
        if (source == null) {
            return;
        }
        Set<Map.Entry<String, JsonNode>> properties = source.properties();
        for (var entry : properties) {
            String fieldName = entry.getKey();
            JsonNode sourceValue = entry.getValue();

            JsonNode targetValue = target.get(fieldName);

            if (targetValue != null
                    && targetValue.isObject()
                    && sourceValue.isObject()) {

                mergeInto((ObjectNode) targetValue, (ObjectNode) sourceValue);

            } else {
                target.set(fieldName, sourceValue);
            }
        }
    }
}
