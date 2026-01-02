package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.poc.commons.settings.ConfigurationRequest;
import io.github.jlmc.poc.commons.settings.IndustriesSettingsClient;
import io.github.jlmc.poc.commons.settings.json.JacksonJsonDeserializer;
import io.github.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.github.jlmc.poc.commons.settings.redis.DefaultRedisExecutionStrategy;
import io.github.jlmc.poc.commons.settings.redis.RedisExecutionStrategy;
import io.github.jlmc.poc.commons.settings.redis.RedisL1L2SimpleMap;
import io.github.jlmc.poc.commons.settings.redis.keys.KeyBuilder;
import io.github.jlmc.poc.commons.settings.redis.keys.StandardKeyBuilder;
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

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(SettingsConfigurationProperties.class)
public class SettingsConfiguration {

    //@Autowired
    //RedisConfiguration redisConfiguration;

    //@Autowired
    //RedisConnectionFactory connectionFactory;

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

    // Beans for decryption
    // this beans should be optional and only created when decryption is needed and the redis is enabled


    @Bean
    @ConditionalOnClass({
            RedisConnectionFactory.class,
            io.lettuce.core.RedisClient.class,
            org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory.class})
    //@ConditionalOnBean(RedisConnectionFactory.class)
    // tdk.configurations-settings.redis-enabled
    @ConditionalOnProperty(prefix = "tdk.configurations-settings", name = "redis-enabled", havingValue = "true")
    @ConditionalOnMissingBean
    public RedisExecutionStrategy redisExecutionStrategy(
            SettingsConfigurationProperties properties,
            RedisConnectionFactory connectionFactory,
            JsonDeserializer jsonDeserializer,
            ObjectMapper objectMapper
    ) {

        io.lettuce.core.RedisClient redisClient =
                (io.lettuce.core.RedisClient) ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) connectionFactory)
                        .getNativeClient();

        if (redisClient == null) {
            throw new IllegalStateException("RedisClient is null, ensure that you are using LettuceConnectionFactory");
        }

        String namespace = properties.namespace();
        Duration duration = properties.redisL1Ttl();
        RedisL1L2SimpleMap redisL1SimpleMap = new RedisL1L2SimpleMap(redisClient, namespace, duration.toMinutes(), TimeUnit.MINUTES);

        KeyBuilder keyBuilder = new StandardKeyBuilder(namespace, ConfigurationRequest::accountId);

        ResolvedConfigurationAssembler resolvedConfigurationAssembler = ResolvedConfigurationAssembler.defaultAssembler(objectMapper);

        return new DefaultRedisExecutionStrategy(
                redisL1SimpleMap,
                keyBuilder,
                jsonDeserializer,
                resolvedConfigurationAssembler
        );
    }
}
