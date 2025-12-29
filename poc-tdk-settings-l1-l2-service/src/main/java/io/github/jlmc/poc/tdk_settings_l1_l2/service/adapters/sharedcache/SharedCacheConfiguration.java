package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.noop.NoOpSharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.redis.RedisSharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SharedCacheConfigurationProperties.class)
public class SharedCacheConfiguration {

    @Bean
    public SharedCacheSynchronizer sharedCacheSynchronizer(SharedCacheConfigurationProperties properties) {
        return switch (properties.type()) {
            case NOOP -> new NoOpSharedCacheSynchronizer();
            case REDIS -> new RedisSharedCacheSynchronizer();
            case null, default ->
                    throw new IllegalStateException("Unsupported SharedCacheSynchronizer type: " + properties.type());
        };
    }

}
