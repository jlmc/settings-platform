# 🍃 settings-client-spring-boot-starter

This Spring Boot Starter facilitates the integration of the `settings-client` SDK into Spring Boot applications by providing auto-configuration and easy setup.

## 📦 Features

- **Auto-configuration**: Automatically configures the `IndustriesSettingsClient` bean.
- **Redis Integration**: Automatically sets up Redis-based caching if Redis is available on the classpath.
- **Caffeine L1 Cache**: Configures Caffeine as an L1 cache when the dependency is present.
- **Property-based Configuration**: Easily configure the client via `application.yaml` or `application.properties`.

## 🚀 Usage

### 1. Add Dependency

Add the starter to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.jlmc</groupId>
    <artifactId>settings-client-spring-boot-starter</artifactId>
    <version>${project.version}</version>
</dependency>
```

### 2. Configure Properties

Configure the client in your `application.yaml`:

```yaml
industries:
  settings:
    client:
      api-base-url: "https://api.settings-service.com"
      namespace: "my-app-namespace"
      redis-enabled: true
      redis-l1-ttl: "PT10M"
      redis-l1-max-size: 1000
```

### 3. Inject the Client

You can now inject `IndustriesSettingsClient` into your beans:

```java
@Service
public class MyService {
    private final IndustriesSettingsClient settingsClient;

    public MyService(IndustriesSettingsClient settingsClient) {
        this.settingsClient = settingsClient;
    }
}
```

## 🛠️ Configuration Properties

| Property                                        | Description                                    | Default    |
|-------------------------------------------------|------------------------------------------------|------------|
| `industries.settings.client.api-base-url`       | The base URL of the settings service.          | (Required) |
| `industries.settings.client.namespace`          | Redis namespace for caching.                   | `settings` |
| `industries.settings.client.redis-enabled`      | Enable Redis-based L2 caching.                 | `true`     |
| `industries.settings.client.redis-l1-ttl`       | TTL for L1 Caffeine cache (ISO-8601 duration). | `PT10H`    |
| `industries.settings.client.redis-l1-max-size`  | Max size for L1 Caffeine cache.                | `1000`     |
| `industries.settings.client.connection-timeout` | HTTP connection timeout (ISO-8601 duration).   | `PT2S`     |
| `industries.settings.client.request-timeout`    | HTTP request timeout (ISO-8601 duration).      | `PT5S`     |
