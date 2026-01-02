package io.github.jlmc.settings.service.adapters.json;

import io.github.jlmc.settings.service.domain.ports.ObjectNodeMerger;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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
