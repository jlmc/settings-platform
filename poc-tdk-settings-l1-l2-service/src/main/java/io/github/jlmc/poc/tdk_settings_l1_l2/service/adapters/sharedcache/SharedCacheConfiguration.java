package io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache;

import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.noop.NoOpSharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.adapters.sharedcache.redis.RedisSharedCacheSynchronizer;
import io.github.jlmc.poc.tdk_settings_l1_l2.service.domain.ports.SharedCacheSynchronizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(SharedCacheConfigurationProperties.class)
public class SharedCacheConfiguration {

    @Bean
    @ConditionalOnProperty(name = "sharedcache.type", havingValue = "NOOP", matchIfMissing = true)
    public SharedCacheSynchronizer noOpSharedCacheSynchronizer() {
        return new NoOpSharedCacheSynchronizer();
    }

    @Bean
    @ConditionalOnProperty(name = "sharedcache.type", havingValue = "REDIS")
    public SharedCacheSynchronizer redisSharedCacheSynchronizer(RedisTemplate<String, Object> redisTemplate) {
        return new RedisSharedCacheSynchronizer(redisTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "sharedcache.type", havingValue = "REDIS")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        GenericJacksonJsonRedisSerializer jacksonJsonRedisSerializer = new GenericJacksonJsonRedisSerializer(objectMapper);
        template.setValueSerializer(jacksonJsonRedisSerializer);

        template.setHashValueSerializer(jacksonJsonRedisSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

}
