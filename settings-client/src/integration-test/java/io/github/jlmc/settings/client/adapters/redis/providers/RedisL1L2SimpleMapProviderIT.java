package io.github.jlmc.settings.client.adapters.redis.providers;

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

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Testcontainers(disabledWithoutDocker = true)
class RedisL1L2SimpleMapProviderIT {

    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.4.2-alpine"))
                    .withExposedPorts(6379);

    private RedisL1L2SimpleMapProvider victim;
    private RedisClient redisClient;
    private final RedisKeyGenerator keyGenerator = new RedisKeyGenerator("test-namespace");

    @BeforeEach
    void setUp() {
        String redisUri = String.format("redis://%s:%d", redis.getHost(), redis.getMappedPort(6379));
        redisClient = RedisClient.create(redisUri);
        victim = new RedisL1L2SimpleMapProvider(redisClient, keyGenerator.namespace());
    }

    @AfterEach
    void tearDown() {
        if (victim != null) {
            victim.close();
        }
    }

    @Test
    @DisplayName("Should return null when key does not exist")
    void shouldReturnNullWhenKeyDoesNotExist() {
        String value = victim.getValue("non-existent-key");

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return value and populate L1 when key exists in Redis")
    void shouldReturnValueAndPopulateL1WhenKeyExists() {
        String fullKey = keyGenerator.generateFullKey("existing-key");
        String expectedValue = "{\"name\":\"test\"}";
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(fullKey, expectedValue);
        }

        // 1st call - should fetch from Redis and populate L1
        String actualValue1 = victim.getValue(fullKey);
        assertThat(actualValue1).isEqualTo(expectedValue);
        assertThat(getL1Cache().get(fullKey)).isEqualTo(expectedValue);

        // 2nd call - should come from L1
        String actualValue2 = victim.getValue(fullKey);
        assertThat(actualValue2).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should invalidate L1 when key is updated in Redis")
    void shouldInvalidateL1WhenKeyIsUpdated() {
        String fullKey = keyGenerator.generateFullKey("test-key");
        String initialValue = "initial";
        String updatedValue = "updated";

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(fullKey, initialValue);
        }

        // Populate L1
        victim.getValue(fullKey);
        assertThat(getL1Cache().get(fullKey)).isEqualTo(initialValue);

        // Update in Redis via another connection
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(fullKey, updatedValue);
        }

        // Wait a bit for invalidation message to arrive (Server-side tracking is asynchronous)
        // L1 should be empty or at least not contain the old value for the key
        await().atMost(Duration.ofSeconds(2))
                .pollInterval(Duration.ofMillis(50))
                .untilAsserted(() -> assertThat(getL1Cache().get(fullKey)).isNull());

        assertThat(getL1Cache().get(fullKey)).isNull();

        // Should fetch new value from Redis
        String actualValue = victim.getValue(fullKey);
        assertThat(actualValue).isEqualTo(updatedValue);
        assertThat(getL1Cache().get(fullKey)).isEqualTo(updatedValue);
    }

    @Test
    @DisplayName("Should return null and handle exception when Redis is unavailable")
    void shouldHandleRedisUnavailability() {
        victim.close();

        String value = victim.getValue(keyGenerator.generateFullKey("any-key"));

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return the namespace")
    void shouldReturnNamespace() {
        assertThat(victim.getNamespace()).isEqualTo(keyGenerator.namespace());
    }

    @Test
    @DisplayName("Should check availability correctly")
    void shouldCheckAvailability() {
        assertThat(victim.isAvailable()).isTrue();

        victim.close();

        assertThat(victim.isAvailable()).isFalse();
    }

    private Map<String, String> getL1Cache() {
        return victim.mapCache;
        /*
        Field field = RedisL1L2SimpleMapProvider.class.getDeclaredField("mapCache");
        field.setAccessible(true);
        return (Map<String, String>) field.get(provider);
         */
    }
}
