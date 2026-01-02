package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.poc.commons.settings.ConfigurationRequest;
import io.github.jlmc.poc.commons.settings.IndustriesSettingsClient;
import io.github.jlmc.poc.commons.settings.json.JacksonJsonDeserializer;
import io.github.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.github.jlmc.poc.commons.settings.redis.DefaultRedisExecutionStrategy;
import io.github.jlmc.poc.commons.settings.redis.RedisExecutionStrategy;
import io.github.jlmc.poc.commons.settings.redis.RedisSettingsProvider;
import io.github.jlmc.poc.commons.settings.redis.keys.KeyBuilder;
import io.github.jlmc.poc.commons.settings.redis.keys.StandardKeyBuilder;
import io.github.jlmc.poc.commons.settings.redis.providers.RedisL1L2Caffeine;
import io.github.jlmc.poc.commons.settings.redis.providers.RedisL2OnlyService;
import io.github.jlmc.poc.settings.sdk.domain.ResolvedConfigurationAssembler;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.ports.IndustriesSettingsProviderPort;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(SettingsConfigurationProperties.class)
public class SettingsConfiguration {

    @Bean
    public IndustriesSettingsClient industriesSettingsClient(
            SettingsConfigurationProperties properties,
            JsonDeserializer jsonDeserializer,
            ObjectProvider<RedisExecutionStrategy> redisExecutionStrategyProvider
    ) {

        RedisExecutionStrategy redisExecutionStrategy = redisExecutionStrategyProvider.getIfAvailable();

        IndustriesSettingsClient industriesSettingsClient =
                IndustriesSettingsClient.builder()
                        .apiBaseUrl(properties.apiBaseUrl())
                        .jsonDeserializer(jsonDeserializer)
                        .useRetryExecutor(false)
                        .userAgent("tdk-l1-l2-app-webflux")
                        .redisExecutionStrategy(redisExecutionStrategy)
                        .build();

        return industriesSettingsClient;
    }

    @Bean
    public JsonDeserializer jsonDeserializer(ObjectMapper objectMapper) {
        return new JacksonJsonDeserializer(objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public IndustriesSettingsProviderPort industriesSettingsProviderPort(IndustriesSettingsClient industriesSettingsClient) {
        return new DefaultIndustriesSettingsProviderPort(industriesSettingsClient);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tdk.configurations-settings", name = "redis-enabled", havingValue = "true")
    @ConditionalOnClass(com.github.benmanes.caffeine.cache.Cache.class)
    public RedisSettingsProvider redisL1L2CaffeineSettingsProvider(
            SettingsConfigurationProperties properties,
            RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient = getRedisClient(connectionFactory);
        return new RedisL1L2Caffeine(
                redisClient,
                properties.namespace(),
                properties.redisL1Ttl().toMinutes(),
                TimeUnit.MINUTES);
    }

    @Bean
    @ConditionalOnProperty(prefix = "tdk.configurations-settings", name = "redis-enabled", havingValue = "true")
    @ConditionalOnMissingBean(RedisSettingsProvider.class)
    public RedisSettingsProvider redisL2OnlySettingsProvider(
            SettingsConfigurationProperties properties,
            RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient = getRedisClient(connectionFactory);
        return new RedisL2OnlyService(redisClient, properties.namespace());
    }

    @Bean
    @ConditionalOnClass({
            RedisConnectionFactory.class,
            io.lettuce.core.RedisClient.class,
            org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory.class})
    @ConditionalOnProperty(prefix = "tdk.configurations-settings", name = "redis-enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public RedisExecutionStrategy redisExecutionStrategy(
            SettingsConfigurationProperties properties,
            RedisSettingsProvider redisSettingsProvider,
            JsonDeserializer jsonDeserializer,
            ObjectMapper objectMapper
    ) {
        String namespace = properties.namespace();

        KeyBuilder keyBuilder = new StandardKeyBuilder(namespace, ConfigurationRequest::accountId);

        ResolvedConfigurationAssembler resolvedConfigurationAssembler = ResolvedConfigurationAssembler.defaultAssembler(objectMapper);

        return new DefaultRedisExecutionStrategy(
                redisSettingsProvider,
                keyBuilder,
                jsonDeserializer,
                resolvedConfigurationAssembler
        );
    }

    private static io.lettuce.core.RedisClient getRedisClient(RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient =
                (io.lettuce.core.RedisClient) ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) connectionFactory)
                        .getNativeClient();

        if (redisClient == null) {
            throw new IllegalStateException("RedisClient is null, ensure that you are using LettuceConnectionFactory");
        }
        return redisClient;
    }
}
