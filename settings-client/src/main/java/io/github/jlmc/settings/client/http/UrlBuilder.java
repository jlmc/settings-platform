package io.github.jlmc.settings.client.http;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable, thread-safe, production-ready URL builder.
 * <p>
 * Supports:
 * - Base path
 * - Path segments
 * - Query parameters (multi-value)
 * - Matrix parameters per segment
 * - URL encoding toggle
 * - Defensive copying to prevent external mutation
 */
public final class UrlBuilder {

    private final String basePath;
    private final List<String> pathSegments;
    private final Map<String, List<String>> queryParams;
    private final Map<String, Map<String, String>> matrixParams;
    private final boolean encode;

    private UrlBuilder(
            String basePath,
            List<String> pathSegments,
            Map<String, List<String>> queryParams,
            Map<String, Map<String, String>> matrixParams,
            boolean encode
    ) {
        this.basePath = normalizeBasePath(basePath);
        this.pathSegments = List.copyOf(pathSegments);
        this.queryParams = deepUnmodifiableCopy(queryParams);
        this.matrixParams = deepUnmodifiableMatrixCopy(matrixParams);
        this.encode = encode;
    }

    // ----------------------
    // Factory method
    // ----------------------
    public static UrlBuilder create() {
        return new UrlBuilder("", List.of(), Map.of(), Map.of(), true);
    }

    // ----------------------
    // Internal helpers
    // ----------------------
    private static String normalizeBasePath(String path) {
        return path == null ? "" : path.replaceAll("/$", "");
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static Map<String, List<String>> deepUnmodifiableCopy(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        source.forEach((k, v) -> copy.put(k, List.copyOf(v)));
        return Collections.unmodifiableMap(copy);
    }

    private static Map<String, Map<String, String>> deepUnmodifiableMatrixCopy(Map<String, Map<String, String>> source) {
        Map<String, Map<String, String>> copy = new LinkedHashMap<>();
        source.forEach((k, v) -> copy.put(k, Collections.unmodifiableMap(new LinkedHashMap<>(v))));
        return Collections.unmodifiableMap(copy);
    }

    // ----------------------
    // Base path
    // ----------------------
    public UrlBuilder withBasePath(String basePath) {
        return new UrlBuilder(basePath, pathSegments, queryParams, matrixParams, encode);
    }

    // ----------------------
    // Path segment
    // ----------------------
    public UrlBuilder path(String segment) {
        if (isBlank(segment)) return this;
        List<String> newSegments = new ArrayList<>(pathSegments);
        newSegments.add(segment);
        return new UrlBuilder(basePath, newSegments, queryParams, matrixParams, encode);
    }

    // ----------------------
    // Query parameter (multi-value)
    // ----------------------
    public UrlBuilder queryParam(String name, String value) {
        if (isBlank(name) || value == null) return this;

        Map<String, List<String>> newQueryParams = new LinkedHashMap<>();
        queryParams.forEach((k, v) -> newQueryParams.put(k, new ArrayList<>(v)));
        newQueryParams.computeIfAbsent(name, k -> new ArrayList<>()).add(value);

        return new UrlBuilder(basePath, pathSegments, newQueryParams, matrixParams, encode);
    }

    // ----------------------
    // Matrix parameter for specific path segment
    // ----------------------
    public UrlBuilder matrixParam(String segment, String name, String value) {
        if (isBlank(segment) || isBlank(name) || value == null) return this;

        Map<String, Map<String, String>> newMatrixParams = new LinkedHashMap<>();
        matrixParams.forEach((k, v) -> newMatrixParams.put(k, new LinkedHashMap<>(v)));
        newMatrixParams.computeIfAbsent(segment, k -> new LinkedHashMap<>()).put(name, value);

        return new UrlBuilder(basePath, pathSegments, queryParams, newMatrixParams, encode);
    }

    // ----------------------
    // Encoding toggle
    // ----------------------
    public UrlBuilder encode(boolean encode) {
        return new UrlBuilder(basePath, pathSegments, queryParams, matrixParams, encode);
    }

    // ----------------------
    // Build final URL string
    // ----------------------
    public String build() {
        StringBuilder sb = new StringBuilder();
        if (!isBlank(basePath)) {
            sb.append(basePath.replaceAll("/$", ""));
        }

        for (String segment : pathSegments) {
            sb.append("/");
            sb.append(encode ? urlEncode(segment) : segment);

            // matrix params for this segment
            Map<String, String> matrices = matrixParams.get(segment);
            if (matrices != null) {
                matrices.forEach((k, v) -> sb.append(";").append(k).append("=").append(encode ? urlEncode(v) : v));
            }
        }

        if (!queryParams.isEmpty()) {
            sb.append("?");
            queryParams.forEach((k, vList) -> {
                for (String v : vList) {
                    sb.append(encode ? urlEncode(k) : k)
                            .append("=")
                            .append(encode ? urlEncode(v) : v)
                            .append("&");
                }
            });
            sb.setLength(sb.length() - 1); // remove trailing &
        }

        return sb.toString();
    }

    // ----------------------
    // Convert to URI
    // ----------------------
    public URI toURI() {
        return URI.create(build());
    }

    // ----------------------
    // Expose defensive copies for inspection
    // ----------------------
    public List<String> pathSegments() {
        return List.copyOf(pathSegments);
    }

    public Map<String, List<String>> queryParams() {
        return deepUnmodifiableCopy(queryParams);
    }

    public Map<String, Map<String, String>> matrixParams() {
        return deepUnmodifiableMatrixCopy(matrixParams);
    }

    public boolean encode() {
        return encode;
    }

    public String basePath() {
        return basePath;
    }
}
