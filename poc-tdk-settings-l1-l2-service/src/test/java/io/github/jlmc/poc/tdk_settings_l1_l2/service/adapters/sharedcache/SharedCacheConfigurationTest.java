package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.noop.NoOpSharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.redis.RedisSharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SharedCacheConfigurationTest {

    private final SharedCacheConfiguration victim = new SharedCacheConfiguration();

    @Test
    void when_type_is_no_ops_the_returned_type_should_be_NoOpSharedCacheSynchronizer() {
        SharedCacheConfigurationProperties properties = new SharedCacheConfigurationProperties(
                SharedCacheConfigurationProperties.Type.NOOP,
                Duration.ofDays(1),
                null
        );

        SharedCacheSynchronizer result = victim.sharedCacheSynchronizer(properties);

        assertNotNull(result);
        assertInstanceOf(NoOpSharedCacheSynchronizer.class, result);
    }

    @Test
    void when_type_is_redis_the_returned_type_should_be_RedisSharedCacheSynchronizer() {
        SharedCacheConfigurationProperties properties = new SharedCacheConfigurationProperties(
                SharedCacheConfigurationProperties.Type.REDIS,
                Duration.ofDays(1),
                new SharedCacheConfigurationProperties.Redis("localhost", 6379)
        );

        SharedCacheSynchronizer result = victim.sharedCacheSynchronizer(properties);

        assertNotNull(result);
        assertInstanceOf(RedisSharedCacheSynchronizer.class, result);
    }

    @Test
    void when_type_is_null_the_returned_type_should_throw_exception() {
        SharedCacheConfigurationProperties properties = new SharedCacheConfigurationProperties(
                null,
                Duration.ofDays(1),
                null
        );

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> victim.sharedCacheSynchronizer(properties));

        Assertions.assertEquals("Unsupported SharedCacheSynchronizer type: null", ex.getMessage());

    }
}
