package io.github.jlmc.settings.client.adapters.redis.providers;

import io.lettuce.core.RedisClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RedisL2OnlyProviderIT {

    // alternatively you can use the RedisContainer from Testcontainers library
    // static RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:7.4.2-alpine"));
    @SuppressWarnings("resource")
    @Container
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7.4.2-alpine"))
                    .withExposedPorts(6379);

    private RedisL2OnlyProvider victim;
    private RedisClient redisClient;
    private final RedisKeyGenerator keyGenerator = new RedisKeyGenerator("test-namespace");

    @BeforeEach
    void setUp() {
        String redisUri = String.format("redis://%s:%d", redis.getHost(), redis.getMappedPort(6379));
        redisClient = RedisClient.create(redisUri);
        victim = new RedisL2OnlyProvider(redisClient, keyGenerator.namespace());
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
        String value = victim.getValue(keyGenerator.generateFullKey("non-existent-key"));

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return value when key exists in Redis")
    void shouldReturnValueWhenKeyExists() {
        String fullKey = keyGenerator.generateFullKey("existing-key");
        String expectedValue = "{\"name\":\"test\"}";
        redisClient.connect().sync().set(fullKey, expectedValue);

        String actualValue = victim.getValue(fullKey);

        assertThat(actualValue).isEqualTo(expectedValue);
    }

    @Test
    @DisplayName("Should return null when key is null or blank")
    void shouldReturnNullWhenKeyIsInvalid() {
        assertThat(victim.getValue(null)).isNull();
        assertThat(victim.getValue("")).isNull();
        assertThat(victim.getValue("   ")).isNull();
    }

    @Test
    @DisplayName("Should return null and handle exception when Redis is unavailable")
    void shouldHandleRedisUnavailability() {
        // Close the provider to simulate unavailability (it also shuts down the client)
        victim.close();

        String value = victim.getValue("any-key");

        assertThat(value).isNull();
    }

    @Test
    @DisplayName("Should return the namespace")
    void shouldReturnNamespace() {
        assertThat(victim.getNamespace()).isEqualTo("test-namespace");
    }

    @Test
    @DisplayName("Should check availability correctly")
    void shouldCheckAvailability() {
        assertThat(victim.isAvailable()).isTrue();

        victim.close();

        assertThat(victim.isAvailable()).isFalse();
    }
}
