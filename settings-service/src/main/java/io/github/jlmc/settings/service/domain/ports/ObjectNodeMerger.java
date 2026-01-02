package io.github.jlmc.settings.service.domain.ports;

import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface ObjectNodeMerger {

    ObjectNode mergeContents(List<Supplier<ObjectNode>> sources);
    Map<String, Object> mergeContentsAsMap(List<Supplier<ObjectNode>> sources);

}
