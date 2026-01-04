package io.github.jlmc.settings.se.example.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jlmc.settings.client.ConfigurationRequest;
import io.github.jlmc.settings.client.IndustriesSettingsClient;
import io.github.jlmc.settings.client.json.JacksonJsonDeserializer;
import io.github.jlmc.settings.client.json.JsonDeserializer;
import io.github.jlmc.settings.client.redis.DistributedConfigProvider;
import io.github.jlmc.settings.client.redis.RedisDistributedConfigProvider;
import io.github.jlmc.settings.client.redis.keys.StandardKeyBuilder;
import io.github.jlmc.settings.client.redis.providers.RedisL1L2CaffeineProvider;
import io.github.jlmc.settings.domain.components.ResolvedConfigurationAssembler;
import io.github.jlmc.settings.se.example.adapter.out.config.IndustriesSettingsAdapter;
import io.github.jlmc.settings.se.example.application.in.GetConfigurationUseCase;
import io.github.jlmc.settings.se.example.application.out.ConfigurationPort;
import io.github.jlmc.settings.se.example.application.service.GetConfigurationService;
import io.lettuce.core.RedisClient;

import java.util.concurrent.TimeUnit;

public class ApplicationConfig {

    public static GetConfigurationUseCase getConfigurationUseCase(
            String namespace,
            String redisUri,
            String apiBaseUrl
    ) {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

        JsonDeserializer jacksonJsonDeserializer = new JacksonJsonDeserializer(objectMapper);


        RedisClient redisClient = RedisClient.create(redisUri);

        StandardKeyBuilder keyBuilder = new StandardKeyBuilder(namespace, ConfigurationRequest::accountId);
        
        DistributedConfigProvider redisProvider =
                new RedisDistributedConfigProvider(
                        new RedisL1L2CaffeineProvider(
                                redisClient,
                                namespace,
                                10,
                                TimeUnit.MINUTES,
                                500
                        ),
                        keyBuilder,
                        jacksonJsonDeserializer,
                        ResolvedConfigurationAssembler.defaultAssembler(objectMapper)
                );

        //RetryExecutor retryExecutor = Resilience4jRetryExecutor.defaultRetryExecutor();

        IndustriesSettingsClient client =
                IndustriesSettingsClient.builder()
                        .apiBaseUrl(apiBaseUrl)
                        .userAgent("settings-se-example-app/1.0.0")
                        .useRetryExecutor(true)
                        //.retryExecutor(retryExecutor)
                        .redisExecutionStrategy(redisProvider)
                        .build();

        ConfigurationPort configurationPort = new IndustriesSettingsAdapter(client);

        return new GetConfigurationService(configurationPort);
    }

}
