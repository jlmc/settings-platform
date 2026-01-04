package io.github.jlmc.settings.webflux.example.adapters.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.settings.client.ConfigurationRequest;
import io.github.jlmc.settings.client.IndustriesSettingsClient;
import io.github.jlmc.settings.client.json.JacksonJsonDeserializer;
import io.github.jlmc.settings.client.json.JsonDeserializer;
import io.github.jlmc.settings.client.redis.DistributedConfigProvider;
import io.github.jlmc.settings.client.redis.RedisDistributedConfigProvider;
import io.github.jlmc.settings.client.redis.RedisSettingsProvider;
import io.github.jlmc.settings.client.redis.keys.KeyBuilder;
import io.github.jlmc.settings.client.redis.keys.StandardKeyBuilder;
import io.github.jlmc.settings.client.redis.providers.RedisL1L2CaffeineProvider;
import io.github.jlmc.settings.client.redis.providers.RedisL2OnlyProvider;
import io.github.jlmc.settings.domain.components.ResolvedConfigurationAssembler;
import io.github.jlmc.settings.webflux.example.domain.ports.IndustriesSettingsProviderPort;
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
            ObjectProvider<DistributedConfigProvider> redisExecutionStrategyProvider
    ) {

        DistributedConfigProvider distributedConfigProvider = redisExecutionStrategyProvider.getIfAvailable();

        return IndustriesSettingsClient.builder()
                .apiBaseUrl(properties.apiBaseUrl())
                .jsonDeserializer(jsonDeserializer)
                .useRetryExecutor(false)
                .userAgent("webflux-app-example")
                .redisExecutionStrategy(distributedConfigProvider)
                .build();
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
    @ConditionalOnProperty(prefix = "settings.webflux-app.client", name = "redis-enabled", havingValue = "true")
    @ConditionalOnClass(com.github.benmanes.caffeine.cache.Cache.class)
    public RedisSettingsProvider redisL1L2CaffeineSettingsProvider(
            SettingsConfigurationProperties properties,
            RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient = getRedisClient(connectionFactory);
        return new RedisL1L2CaffeineProvider(
                redisClient,
                properties.namespace(),
                properties.redisL1Ttl().toMinutes(),
                TimeUnit.MINUTES,
                properties.redisL1MaxSize());
    }

    @Bean
    @ConditionalOnProperty(prefix = "settings.webflux-app.client", name = "redis-enabled", havingValue = "true")
    @ConditionalOnMissingBean(RedisSettingsProvider.class)
    public RedisSettingsProvider redisL2OnlySettingsProvider(
            SettingsConfigurationProperties properties,
            RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient = getRedisClient(connectionFactory);
        return new RedisL2OnlyProvider(redisClient, properties.namespace());
    }

    @Bean
    @ConditionalOnClass({
            RedisConnectionFactory.class,
            io.lettuce.core.RedisClient.class,
            org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory.class})
    @ConditionalOnProperty(prefix = "settings.webflux-app.client", name = "redis-enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public DistributedConfigProvider redisExecutionStrategy(
            SettingsConfigurationProperties properties,
            RedisSettingsProvider redisSettingsProvider,
            JsonDeserializer jsonDeserializer,
            ObjectMapper objectMapper
    ) {
        String namespace = properties.namespace();

        KeyBuilder keyBuilder = new StandardKeyBuilder(namespace, ConfigurationRequest::accountId);

        ResolvedConfigurationAssembler resolvedConfigurationAssembler = ResolvedConfigurationAssembler.defaultAssembler(objectMapper);

        return new RedisDistributedConfigProvider(
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
