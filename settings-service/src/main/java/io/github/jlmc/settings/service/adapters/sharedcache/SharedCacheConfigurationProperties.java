package io.github.jlmc.settings.service.adapters.sharedcache;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuration properties for the shared cache system.
 * <p>
 * This configuration supports two types of caching:
 * <ul>
 *     <li>{@link Type#NOOP} - No caching is performed.</li>
 *     <li>{@link Type#REDIS} - Redis is used as the caching backend.</li>
 * </ul>
 * <p>
 * <b>Binding:</b> This record is automatically bound from properties prefixed with
 * {@code tdk.shared-cache} in your {@code application.yml} or {@code application.properties}.
 * <p>
 * <b>Validation:</b>
 * <ul>
 *     <li>{@code type} must not be null.</li>
 *     <li>{@code ttl} must not be null.</li>
 *     <li>If {@code type == REDIS}, {@code redis} must not be null.</li>
 *     <li>{@code redis.host} must not be null, {@code redis.port} must be positive.</li>
 * </ul>
 * <p>
 * <b>Example YAML configuration:</b>
 * <pre>{@code
 * tdk:
 *   shared-cache:
 *     type: REDIS
 *     ttl: 7d
 *     redis:
 *       host: redis-host
 *       port: 6379
 * }</pre>
 * <p>
 * <b>Usage in a Spring Bean:</b>
 * <pre>{@code
 * @Service
 * public class CacheService {
 *
 *     private final SharedCacheConfigurationProperties config;
 *
 *     public CacheService(SharedCacheConfigurationProperties config) {
 *         this.config = config;
 *     }
 *
 *     public void printConfig() {
 *         System.out.println("Cache type: " + config.type());
 *         System.out.println("TTL: " + config.ttl());
 *         if (config.type() == SharedCacheConfigurationProperties.Type.REDIS) {
 *             System.out.println("Redis host: " + config.redis().host());
 *             System.out.println("Redis port: " + config.redis().port());
 *         }
 *     }
 * }
 * }</pre>
 */
@ConfigurationProperties(prefix = "tdk.shared-cache")
public record SharedCacheConfigurationProperties(
        @NotNull @DefaultValue("REDIS") Type type,
        @NotNull @DefaultValue("P7D") Duration ttl,
        @Valid Redis redis
) {

    /**
     * Redis configuration is mandatory only when type == REDIS.
     */
    @AssertTrue(message = "Redis configuration must be provided when shared-cache.type is REDIS")
    public boolean isRedisConfigValid() {
        return type != Type.REDIS || redis != null;
    }

    public enum Type {
        NOOP,
        REDIS
    }

    public record Redis(
            @NotBlank @DefaultValue("settings") String namespace
    ) {
    }
}
