package io.gihub.jlmc.poc.commons.settings.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class UrlBuilderTest {

    @Test
    void shouldBuildSimpleUrlWithBasePathAndSegments() {
        UrlBuilder builder = UrlBuilder.create()
                .withBasePath("https://api.example.com")
                .path("users")
                .path("123");

        String url = builder.build();
        assertEquals("https://api.example.com/users/123", url);
    }

    @Test
    void shouldAddQueryParameters() {
        UrlBuilder builder = UrlBuilder.create()
                .withBasePath("https://api.example.com")
                .path("search")
                .queryParam("q", "john")
                .queryParam("page", "2");

        String url = builder.build();
        assertTrue(url.contains("q=john"));
        assertTrue(url.contains("page=2"));
        assertTrue(url.startsWith("https://api.example.com/search?"));
    }

    @Test
    void shouldSupportMultipleValuesForSameQueryParam() {
        UrlBuilder builder = UrlBuilder.create()
                .path("search")
                .queryParam("filter", "a")
                .queryParam("filter", "b");

        String url = builder.build();
        assertTrue(url.contains("filter=a"));
        assertTrue(url.contains("filter=b"));
        assertTrue(url.indexOf("filter=a") < url.indexOf("filter=b"));
    }

    @Test
    void shouldAddMatrixParams() {
        UrlBuilder builder = UrlBuilder.create()
                .path("users")
                .matrixParam("users", "role", "admin")
                .matrixParam("users", "active", "true");

        String url = builder.build();
        assertTrue(url.startsWith("/users;role=admin;active=true"));
    }

    @Test
    void shouldEncodePathAndQueryParameters() {
        UrlBuilder builder = UrlBuilder.create()
                .path("space here")
                .queryParam("q", "a b");

        String url = builder.build();
        assertTrue(url.contains("space+here") || url.contains("space%20here"));
        assertTrue(url.contains("q=a+b") || url.contains("q=a%20b"));
    }

    @Test
    void shouldDisableEncoding() {
        UrlBuilder builder = UrlBuilder.create()
                .path("space here")
                .queryParam("q", "a b")
                .encode(false);

        String url = builder.build();
        assertTrue(url.contains("space here"));
        assertTrue(url.contains("q=a b"));
    }

    @Test
    void shouldReturnImmutableCopies() {
        UrlBuilder builder = UrlBuilder.create()
                .path("users")
                .queryParam("a", "1")
                .matrixParam("users", "role", "admin");

        assertThrows(UnsupportedOperationException.class, () -> builder.pathSegments().add("fail"));
        assertThrows(UnsupportedOperationException.class, () -> builder.queryParams().put("fail", List.of("x")));
        assertThrows(UnsupportedOperationException.class, () -> builder.matrixParams().put("users", Map.of()));
    }

    @Test
    void shouldConvertToUri() {
        UrlBuilder builder = UrlBuilder.create()
                .withBasePath("https://api.example.com")
                .path("users")
                .path("123");

        URI uri = builder.toURI();
        assertEquals("https://api.example.com/users/123", uri.toString());
    }

    @Test
    void shouldHandleEmptyOrNullSegments() {
        UrlBuilder builder = UrlBuilder.create()
                .path(null)
                .path("")
                .path("valid");

        assertEquals("/valid", builder.build());
    }

    @Test
    void shouldHandleEmptyBasePath() {
        UrlBuilder builder = UrlBuilder.create()
                .withBasePath(null)
                .path("users");

        assertEquals("/users", builder.build());
    }

    @Test
    void shouldNotHaveTrailingQuestionMarkOrAmpersand() {
        UrlBuilder builder = UrlBuilder.create()
                .path("users")
                .queryParam("a", "1");

        String url = builder.build();
        assertFalse(url.endsWith("&"));
        assertFalse(url.endsWith("?"));
    }

    @Test
    void shouldPreserveInsertionOrderOfQueryParams() {
        UrlBuilder builder = UrlBuilder.create()
                .queryParam("z", "0")
                .queryParam("b", "2")
                .queryParam("a", "1")
                .queryParam("c", "3")
                .queryParam("d", "4")
                .queryParam("e", "5");

        String url = builder.build();
        // The query params should appear in the order they were added
        String expectedParams = "z=0&b=2&a=1&c=3&d=4&e=5";
        assertTrue(url.endsWith(expectedParams), "Expected " + expectedParams + " in " + url);
    }

}
