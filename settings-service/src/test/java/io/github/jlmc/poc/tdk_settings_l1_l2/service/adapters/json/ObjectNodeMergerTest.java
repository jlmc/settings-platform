package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonObjectNodeMergerTest {

    private ObjectMapper objectMapper;
    private JacksonObjectNodeMerger victim;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        victim = new JacksonObjectNodeMerger(objectMapper);
    }

    @Test
    void mergeContentsWithEmptySources() {
        ObjectNode result = victim.mergeContents(Collections.emptyList());

        assertTrue(result.isEmpty());
    }

    @Test
    void mergeContentsWithSingleSource() {
        ObjectNode source = objectMapper.createObjectNode().put("key", "value");

        ObjectNode result = victim.mergeContents(List.of(() -> source));

        assertEquals(source, result);
    }

    @Test
    void mergeContentsShallow() {
        ObjectNode source1 = objectMapper.createObjectNode().put("a", 1).put("b", 2);
        ObjectNode source2 = objectMapper.createObjectNode().put("b", 3).put("c", 4);

        ObjectNode result = victim.mergeContents(List.of(() -> source1, () -> source2));

        assertEquals(1, result.get("a").asInt());
        assertEquals(3, result.get("b").asInt());
        assertEquals(4, result.get("c").asInt());
    }

    @Test
    void mergeContentsDeep() {
        ObjectNode source1 = objectMapper.createObjectNode();
        source1.putObject("nested").put("a", 1).put("b", 2);
        
        ObjectNode source2 = objectMapper.createObjectNode();
        source2.putObject("nested").put("b", 3).put("c", 4);

        ObjectNode result = victim.mergeContents(List.of(() -> source1, () -> source2));

        assertEquals(1, result.get("nested").get("a").asInt());
        assertEquals(3, result.get("nested").get("b").asInt());
        assertEquals(4, result.get("nested").get("c").asInt());
    }

    @Test
    void mergeContentsOverwriteNonObjectWithObject() {
        ObjectNode source1 = objectMapper.createObjectNode().put("key", "value");
        ObjectNode source2 = objectMapper.createObjectNode();
        source2.putObject("key").put("inner", "value");

        ObjectNode result = victim.mergeContents(List.of(() -> source1, () -> source2));

        assertTrue(result.get("key").isObject());
        assertEquals("value", result.get("key").get("inner").asText());
    }

    @Test
    void mergeContentsOverwriteObjectWithNonObject() {
        ObjectNode source1 = objectMapper.createObjectNode();
        source1.putObject("key").put("inner", "value");
        ObjectNode source2 = objectMapper.createObjectNode().put("key", "value");

        ObjectNode result = victim.mergeContents(List.of(() -> source1, () -> source2));

        assertTrue(result.get("key").isTextual());
        assertEquals("value", result.get("key").asText());
    }

    @Test
    void mergeContentsWithMultipleNestedLevels() {
        ObjectNode source1 = objectMapper.createObjectNode();
        source1.putObject("level1").putObject("level2").put("key1", "val1");

        ObjectNode source2 = objectMapper.createObjectNode();
        source2.putObject("level1").putObject("level2").put("key2", "val2");

        ObjectNode result = victim.mergeContents(List.of(() -> source1, () -> source2));

        assertEquals("val1", result.path("level1").path("level2").path("key1").asText());
        assertEquals("val2", result.path("level1").path("level2").path("key2").asText());
    }

    @Test
    void mergeContentsWithNullInSourcesList() {
        ObjectNode source1 = objectMapper.createObjectNode().put("key", "value");

        ObjectNode result = victim.mergeContents(java.util.Arrays.asList(() -> source1, () -> null));

        assertEquals("value", result.get("key").asText());
    }

    @Test
    void mergeContentsWithNullSourcesList() {
        ObjectNode result = victim.mergeContents(null);

        assertTrue(result.isEmpty());
    }
}
