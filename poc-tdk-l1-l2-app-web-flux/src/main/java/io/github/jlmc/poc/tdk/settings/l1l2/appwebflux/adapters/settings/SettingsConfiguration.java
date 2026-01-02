package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import io.gihub.jlmc.poc.commons.settings.IndustriesSettingsClient;
import io.gihub.jlmc.poc.commons.settings.json.JacksonJsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.json.JsonDeserializer;
import io.gihub.jlmc.poc.commons.settings.redis.DefaultRedisExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.redis.RedisExecutionStrategy;
import io.gihub.jlmc.poc.commons.settings.redis.RedisL1SimpleMap;
import io.gihub.jlmc.poc.commons.settings.redis.keys.StandardKeyBuilder;
import io.gihub.jlmc.poc.commons.settings.redis.sdk.defaults.JacksonObjectNodeMerger;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.ports.IndustriesSettingsProviderPort;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

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
            RedisExecutionStrategy redisExecutionStrategy
    ) {
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
    public RedisExecutionStrategy redisExecutionStrategy(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            JsonDeserializer jsonDeserializer) {

        io.lettuce.core.RedisClient redisClient =
                (io.lettuce.core.RedisClient) ((org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory) connectionFactory)
                        .getNativeClient();

        if (redisClient == null) {
            throw new IllegalStateException("RedisClient is null, ensure that you are using LettuceConnectionFactory");
        }

        String namespace = "settings";
        RedisL1SimpleMap redisL1SimpleMap =
                new RedisL1SimpleMap(redisClient, namespace);

        StandardKeyBuilder keyBuilder =
                new StandardKeyBuilder(namespace, ConfigurationRequest::accountId);


        // TODO: consider to make JacksonObjectNodeMerger a bean
        JacksonObjectNodeMerger jacksonObjectNodeMerger = new JacksonObjectNodeMerger(objectMapper);

        return new DefaultRedisExecutionStrategy(
                redisL1SimpleMap,
                keyBuilder,
                jsonDeserializer,
                jacksonObjectNodeMerger
        );
    }

    @Bean
    public IndustriesSettingsProviderPort industriesSettingsProviderPort(IndustriesSettingsClient industriesSettingsClient) {
        return new DefaultIndustriesSettingsProviderPort(industriesSettingsClient);
    }


}
