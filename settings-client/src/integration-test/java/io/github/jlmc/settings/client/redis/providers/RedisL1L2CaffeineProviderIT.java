package io.github.jlmc.settings.client.redis.providers;

import com.github.benmanes.caffeine.cache.Cache;
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

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RedisL1L2CaffeineProviderIT {

    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.4.2-alpine"))
                    .withExposedPorts(6379);

    private RedisL1L2CaffeineProvider provider;
    private RedisClient redisClient;
    private final RedisKeyGenerator keyGenerator = new RedisKeyGenerator("test-namespace");

    @BeforeEach
    void setUp() {
        String redisUri = String.format("redis://%s:%d", redis.getHost(), redis.getMappedPort(6379));
        redisClient = RedisClient.create(redisUri);
        provider = new RedisL1L2CaffeineProvider(redisClient, keyGenerator.namespace(), 10, TimeUnit.SECONDS, 100);
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
        String value = provider.getValue(keyGenerator.generateFullKey("non-existent-key"));

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return value and populate L1 when key exists in Redis")
    void shouldReturnValueAndPopulateL1WhenKeyExists() throws Exception {
        String key = keyGenerator.generateFullKey("existing-key");
        String expectedValue = "{\"name\":\"test\"}";
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(key, expectedValue);
        }

        // 1st call - should fetch from Redis and populate L1
        String actualValue1 = provider.getValue(key);
        assertThat(actualValue1).isEqualTo(expectedValue);
        assertThat(getL1Cache().getIfPresent(key)).isEqualTo(expectedValue);

        // 2nd call - should come from L1
        String actualValue2 = provider.getValue(key);
        assertThat(actualValue2).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should invalidate L1 when key is updated in Redis")
    void shouldInvalidateL1WhenKeyIsUpdated() throws Exception {
        String key = keyGenerator.generateFullKey("test-key");
        String initialValue = "initial";
        String updatedValue = "updated";

        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(key, initialValue);
        }

        // Populate L1
        provider.getValue(key);
        assertThat(getL1Cache().getIfPresent(key)).isEqualTo(initialValue);

        // Update in Redis via another connection
        try (StatefulRedisConnection<String, String> connection = redisClient.connect()) {
            connection.sync().set(key, updatedValue);
        }

        // Wait a bit for invalidation message to arrive (Server-side tracking is asynchronous)
        for (int i = 0; i < 20 && getL1Cache().asMap().containsKey(key); i++) {
            Thread.sleep(100);
        }

        // L1 should be empty or at least not contain the old value for the key
        assertThat(getL1Cache().getIfPresent(key)).isNull();

        // Should fetch new value from Redis
        String actualValue = provider.getValue(key);
        assertThat(actualValue).isEqualTo(updatedValue);
        assertThat(getL1Cache().getIfPresent(key)).isEqualTo(updatedValue);
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

    @Test
    @DisplayName("Should evict from L1 when TTL expires")
    void shouldEvictFromL1WhenTtlExpires() throws Exception {
        // Create provider with short TTL
        RedisKeyGenerator shortNamespaceGenerator = new RedisKeyGenerator("short-ttl");
        
        // We need a fresh client because provider.close() shuts down the client
        String redisUri = String.format("redis://%s:%d", redis.getHost(), redis.getMappedPort(6379));
        try (RedisClient shortClient = RedisClient.create(redisUri);
             RedisL1L2CaffeineProvider shortProvider = new RedisL1L2CaffeineProvider(shortClient, shortNamespaceGenerator.namespace(), 1, TimeUnit.SECONDS, 100)) {

            String key = shortNamespaceGenerator.generateFullKey("ttl-key");
            String value = "ttl-value";

            try (StatefulRedisConnection<String, String> connection = shortClient.connect()) {
                connection.sync().set(key, value);
            }

            shortProvider.getValue(key);
            
            // Access the internal cache of the shortProvider
            Field field = RedisL1L2CaffeineProvider.class.getDeclaredField("caffeineCache");
            field.setAccessible(true);
            Cache<String, String> cache = (Cache<String, String>) field.get(shortProvider);
            
            assertThat(cache.getIfPresent(key)).isEqualTo(value);

            // Wait for TTL to expire
            Thread.sleep(1500);

            assertThat(cache.getIfPresent(key)).isNull();
        }
    }

    @Test
    @DisplayName("Should evict from L1 when maximum size is reached")
    void shouldEvictFromL1WhenMaxSizeReached() throws Exception {
        // Create provider with small max size
        RedisKeyGenerator smallNamespaceGenerator = new RedisKeyGenerator("small-max-size");

        // We need a fresh client because provider.close() shuts down the client
        String redisUri = String.format("redis://%s:%d", redis.getHost(), redis.getMappedPort(6379));
        try (RedisClient smallClient = RedisClient.create(redisUri);
             RedisL1L2CaffeineProvider smallProvider = new RedisL1L2CaffeineProvider(smallClient, smallNamespaceGenerator.namespace(), 10, TimeUnit.SECONDS, 1)) {

            String key1 = smallNamespaceGenerator.generateFullKey("key1");
            String key2 = smallNamespaceGenerator.generateFullKey("key2");

            try (StatefulRedisConnection<String, String> connection = smallClient.connect()) {
                connection.sync().set(key1, "val1");
                connection.sync().set(key2, "val2");
            }

            smallProvider.getValue(key1);
            
            Field field = RedisL1L2CaffeineProvider.class.getDeclaredField("caffeineCache");
            field.setAccessible(true);
            Cache<String, String> cache = (Cache<String, String>) field.get(smallProvider);
            
            assertThat(cache.getIfPresent(key1)).isEqualTo("val1");

            // Caffeine's eviction is eventual, but adding another should trigger it
            smallProvider.getValue(key2);

            // Wait a bit for eviction to happen
            for (int i = 0; i < 20 && cache.asMap().containsKey(key1); i++) {
                Thread.sleep(100);
                cache.cleanUp(); // Encourage eviction
            }

            assertThat(cache.asMap()).hasSizeLessThanOrEqualTo(1);
        }
    }

    private Cache<String, String> getL1Cache() {
        return provider.caffeineCache;
        /*
        Field field = RedisL1L2CaffeineProvider.class.getDeclaredField("caffeineCache");
        field.setAccessible(true);
        return (Cache<String, String>) field.get(provider);
         */
    }
}
