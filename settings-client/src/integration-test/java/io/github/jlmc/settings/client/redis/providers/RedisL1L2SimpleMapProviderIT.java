package io.github.jlmc.settings.client.redis.providers;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RedisL1L2SimpleMapProviderIT {

    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.4.2-alpine"))
                    .withExposedPorts(6379);

    private RedisL1L2SimpleMapProvider provider;
    private RedisClient redisClient;
    private RedisKeyGenerator keyGenerator = new RedisKeyGenerator("test-namespace");

    @BeforeEach
    void setUp() {
        String redisUri = String.format("redis://%s:%d", redis.getHost(), redis.getMappedPort(6379));
        redisClient = RedisClient.create(redisUri);
        provider = new RedisL1L2SimpleMapProvider(redisClient, keyGenerator.namespace());
    }

    @AfterEach
    void tearDown() {
        if (provider != null) {
            provider.close();
        }
    }

    @Test
    @DisplayName("Should return null when key does not exist")
    void shouldReturnNullWhenKeyDoesNotExist() {
        String value = provider.getValue("non-existent-key");

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return value and populate L1 when key exists in Redis")
    void shouldReturnValueAndPopulateL1WhenKeyExists() throws Exception {
        String fullKey = keyGenerator.generateFullKey("existing-key");
        String expectedValue = "{\"name\":\"test\"}";
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(fullKey, expectedValue);
        }

        // 1st call - should fetch from Redis and populate L1
        String actualValue1 = provider.getValue(fullKey);
        assertThat(actualValue1).isEqualTo(expectedValue);
        assertThat(getL1Cache().get(fullKey)).isEqualTo(expectedValue);

        // 2nd call - should come from L1
        String actualValue2 = provider.getValue(fullKey);
        assertThat(actualValue2).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should invalidate L1 when key is updated in Redis")
    void shouldInvalidateL1WhenKeyIsUpdated() throws Exception {
        String fullKey = keyGenerator.generateFullKey("test-key");
        String initialValue = "initial";
        String updatedValue = "updated";

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(fullKey, initialValue);
        }

        // Populate L1
        provider.getValue(fullKey);
        assertThat(getL1Cache().get(fullKey)).isEqualTo(initialValue);

        // Update in Redis via another connection
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(fullKey, updatedValue);
        }

        // Wait a bit for invalidation message to arrive (Server-side tracking is asynchronous)
        for (int i = 0; i < 20 && getL1Cache().containsKey(fullKey); i++) {
            Thread.sleep(100);
        }

        // L1 should be empty or at least not contain the old value for the key
        assertThat(getL1Cache().get(fullKey)).isNull();

        // Should fetch new value from Redis
        String actualValue = provider.getValue(fullKey);
        assertThat(actualValue).isEqualTo(updatedValue);
        assertThat(getL1Cache().get(fullKey)).isEqualTo(updatedValue);
    }

    @Test
    @DisplayName("Should return null and handle exception when Redis is unavailable")
    void shouldHandleRedisUnavailability() {
        provider.close();

        String value = provider.getValue(keyGenerator.generateFullKey("any-key"));

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return the namespace")
    void shouldReturnNamespace() {
        assertThat(provider.getNamespace()).isEqualTo(keyGenerator.namespace());
    }

    @Test
    @DisplayName("Should check availability correctly")
    void shouldCheckAvailability() {
        assertThat(provider.isAvailable()).isTrue();

        provider.close();

        assertThat(provider.isAvailable()).isFalse();
    }

    private Map<String, String> getL1Cache() {
        return provider.mapCache;
        /*
        Field field = RedisL1L2SimpleMapProvider.class.getDeclaredField("mapCache");
        field.setAccessible(true);
        return (Map<String, String>) field.get(provider);
         */
    }
}
