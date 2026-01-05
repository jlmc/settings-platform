package io.github.jlmc.settings.client.springboot;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.settings.client.adapters.json.JacksonJsonDeserializer;
import io.github.jlmc.settings.client.adapters.redis.RedisDistributedConfigProvider;
import io.github.jlmc.settings.client.adapters.redis.RedisSettingsProvider;
import io.github.jlmc.settings.client.adapters.redis.keys.KeyBuilder;
import io.github.jlmc.settings.client.adapters.redis.keys.StandardKeyBuilder;
import io.github.jlmc.settings.client.adapters.redis.providers.RedisL1L2CaffeineProvider;
import io.github.jlmc.settings.client.adapters.redis.providers.RedisL2OnlyProvider;
import io.github.jlmc.settings.client.core.Builder;
import io.github.jlmc.settings.client.core.ConfigurationRequest;
import io.github.jlmc.settings.client.core.IndustriesSettingsClient;
import io.github.jlmc.settings.client.ports.out.AccessTokenProvider;
import io.github.jlmc.settings.client.ports.out.DistributedConfigProvider;
import io.github.jlmc.settings.client.ports.out.JsonDeserializer;
import io.github.jlmc.settings.domain.components.ResolvedConfigurationAssembler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@ConditionalOnClass(IndustriesSettingsClient.class)
@EnableConfigurationProperties(SettingsClientProperties.class)
@ConditionalOnProperty(prefix = "industries.settings.client", name = "api-base-url")
public class SettingsClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IndustriesSettingsClient industriesSettingsClient(
            SettingsClientProperties properties,
            JsonDeserializer jsonDeserializer,
            ObjectProvider<AccessTokenProvider> accessTokenProvider,
            ObjectProvider<DistributedConfigProvider> distributedConfigProvider) {
        return IndustriesSettingsClient.builder()
                .apiBaseUrl(properties.getApiBaseUrl())
                .connectionTimeout(properties.getConnectionTimeout())
                .requestTimeout(properties.getRequestTimeout())
                .userAgent(properties.getUserAgent())
                .useRetryExecutor(properties.isUseRetryExecutor())
                .jsonDeserializer(jsonDeserializer)
                .accessTokenProvider(accessTokenProvider.getIfAvailable())
                .redisExecutionStrategy(distributedConfigProvider.getIfAvailable())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonDeserializer settingsJsonDeserializer(ObjectProvider<ObjectMapper> objectMapperProvider) {
        return objectMapperProvider.getIfAvailable() != null
                ? new JacksonJsonDeserializer(objectMapperProvider.getIfAvailable())
                : Builder.defaultJsonDeserializer();
    }

    /*
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(Mono.class)
    public IndustriesSettingsProviderPort industriesSettingsProviderPort(IndustriesSettingsClient industriesSettingsClient) {
        return new DefaultIndustriesSettingsProviderPort(industriesSettingsClient);
    }
     */

    @Bean
    @ConditionalOnProperty(prefix = "industries.settings.client", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnClass({com.github.benmanes.caffeine.cache.Cache.class, RedisConnectionFactory.class})
    public RedisSettingsProvider redisL1L2CaffeineSettingsProvider(
            SettingsClientProperties properties,
            RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient = getRedisClient(connectionFactory);
        return new RedisL1L2CaffeineProvider(
                redisClient,
                properties.getNamespace(),
                properties.getRedisL1Ttl().toMinutes(),
                TimeUnit.MINUTES,
                properties.getRedisL1MaxSize());
    }

    @Bean
    @ConditionalOnProperty(prefix = "industries.settings.client", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(RedisSettingsProvider.class)
    @ConditionalOnClass(RedisConnectionFactory.class)
    public RedisSettingsProvider redisL2OnlySettingsProvider(
            SettingsClientProperties properties,
            RedisConnectionFactory connectionFactory) {
        io.lettuce.core.RedisClient redisClient = getRedisClient(connectionFactory);
        return new RedisL2OnlyProvider(redisClient, properties.getNamespace());
    }

    @Bean
    @ConditionalOnClass({
            RedisConnectionFactory.class,
            io.lettuce.core.RedisClient.class,
            org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory.class})
    @ConditionalOnProperty(prefix = "industries.settings.client", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public DistributedConfigProvider distributedConfigProvider(
            SettingsClientProperties properties,
            RedisSettingsProvider redisSettingsProvider,
            JsonDeserializer jsonDeserializer,
            ObjectProvider<ObjectMapper> objectMapperProvider
    ) {
        String namespace = properties.getNamespace();
        KeyBuilder keyBuilder = new StandardKeyBuilder(namespace, ConfigurationRequest::accountId);

        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable();
        if (objectMapper == null) {
            objectMapper = new ObjectMapper().findAndRegisterModules();
        }
        ResolvedConfigurationAssembler resolvedConfigurationAssembler =
                ResolvedConfigurationAssembler.defaultAssembler(objectMapper);

        return new RedisDistributedConfigProvider(
                redisSettingsProvider,
                keyBuilder,
                jsonDeserializer,
                resolvedConfigurationAssembler
        );
    }

    private static io.lettuce.core.RedisClient getRedisClient(RedisConnectionFactory connectionFactory) {
        if (connectionFactory instanceof org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory lettuceConnectionFactory) {
             io.lettuce.core.RedisClient redisClient = (io.lettuce.core.RedisClient) lettuceConnectionFactory.getNativeClient();
             if (redisClient == null) {
                 throw new IllegalStateException("RedisClient is null, ensure that you are using LettuceConnectionFactory");
             }
             return redisClient;
        }
        throw new IllegalStateException("Only LettuceConnectionFactory is supported for native RedisClient access");
    }
}
