# 📘 settings-client

`settings-client` is a Java/Kotlin SDK for interacting with the **Settings Platform**. It provides capabilities for hierarchical configuration retrieval with support for OAuth2/JWT authentication, RSA decryption, and advanced L1/L2 caching.

> `IndustriesSettingsClient` is the main entry point for interacting with the Settings Platform API.

---

## 🚀 Quick Start

### 1. Add Dependencies

The SDK is designed to be lightweight and modular. You must include the core dependency and choose the optional ones based on your use case.

```xml
<dependency>
    <groupId>io.github.jlmc</groupId>
    <artifactId>settings-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> **Note:** This is a SNAPSHOT version and may change frequently.

#### Mandatory Dependencies

* **`settings-domain`**: Contains domain entities and resolution logic.
* **`jackson-databind`**: Used for JSON serialization/deserialization. (Scope: `provided`, must be available at runtime).

#### Optional Dependencies

| Dependency           | Use Case                                                                                                                                                                                                                                                      |
|:---------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `lettuce-core`       | Required for **Redis (L2)** caching support.                                                                                                                                                                                                                  |
| `caffeine`           | Required for **L1 (In-Memory)** caching (used with `RedisL1L2CaffeineProvider`).                                                                                                                                                                              |
| `nimbus-jose-jwt`    | Required for **JWT-based authentication** (`PrivilegedCredentials`, `PrivilegedCredentialsStrategy`). This dependency is only needed when using `PrivilegedCredentials`. If you only use `BearerTokenCredentials` or `ClientCredentials`, it is not required. |
| `resilience4j-retry` | Required for automatic **HTTP retries**.                                                                                                                                                                                                                      |
| `slf4j-api`          | Used for logging.                                                                                                                                                                                                                                             |

---

### 2. Initialize the Client

The client should be treated as a singleton and reused across the application.

```java
IndustriesSettingsClient client = IndustriesSettingsClient.builder()
        .apiBaseUrl("https://settings.api.example.com")
        .build();
```

---

### 3. Fetch a Configuration

```java
AuthCredentials auth = new ClientCredentials(
        "client-id",
        "secret",
        "https://auth.example.com"
);

ConfigurationRequest request = ConfigurationRequest.standard(
        auth,
        "MyService",
        ConfigurationType.AGENT,
        "agent-123"
);

MyConfig config = client.getConfiguration(request, MyConfig.class);
```

---

## 🏗️ Core Concepts

### Authentication Strategies

* `ClientCredentials`: Standard OAuth2 client credentials flow.
* `PrivilegedCredentials`: RSA-signed JWT flow for internal services (requires `nimbus-jose-jwt`).
* `BearerTokenCredentials`: Uses a pre-existing bearer token for authentication.

### Configuration Hierarchy & Types

Configurations are resolved based on the hierarchy:

**Service > Account > User > Agent**

Supported types:
`SERVICE`, `ACCOUNT`, `USER`, `AGENT`.

---

## ⚡ Advanced Features

### L1/L2 Caching (Redis & Caffeine)

> If you don't need caching, you can skip this section and use the client without any additional configuration.

To enable caching and reduce network latency, configure a `DistributedConfigProvider`.

```java
RedisClient redisClient = RedisClient.create("redis://localhost:6379");

DistributedConfigProvider redisProvider = new RedisDistributedConfigProvider(
    new RedisL1L2CaffeineProvider(
        redisClient,
        "my-app",
        10,
        TimeUnit.MINUTES,
        500
    ),
    new StandardKeyBuilder(),
    Builder.defaultJsonDeserializer(),
    new DefaultResolvedConfigurationAssembler()
);

IndustriesSettingsClient client = IndustriesSettingsClient.builder()
        .apiBaseUrl("https://settings.api.example.com")
        .redisExecutionStrategy(redisProvider)
        .build();
```

#### Implementation Options

| Provider                         | Type    | Description                            | Pros / Cons                                                                                    | Use Case                                               |
|:---------------------------------|:--------|:---------------------------------------|:-----------------------------------------------------------------------------------------------|:-------------------------------------------------------|
| **`RedisL1L2CaffeineProvider`**  | L1 + L2 | Caffeine (L1) + Redis (L2).            | **Pros:** High performance, TTL support, size limits, automatic invalidation via Pub/Sub.      | **Recommended for production.**                        |
| **`RedisL1L2SimpleMapProvider`** | L1 + L2 | `ConcurrentHashMap` (L1) + Redis (L2). | **Cons:** No TTL or size limit for L1. Risk of `OutOfMemory` if too many keys are cached.      | Small sets of configurations (< 1000 elements).        |
| **`RedisL2OnlyProvider`**        | L2 Only | Direct Redis access.                   | **Pros:** No local memory footprint. **Cons:** Higher latency due to a network call per fetch. | Frequently changing data shared across many instances. |

---

### Resilience & Retries

Enable automatic retries for HTTP requests by adding `resilience4j-retry` to the classpath and enabling it in the builder:

```java
IndustriesSettingsClient client = IndustriesSettingsClient.builder()
        .apiBaseUrl(baseUrl)
        .useRetryExecutor(true) // Uses Resilience4j if available
        .build();
```

---

## 🧪 Development & Testing

### Build

```bash
mvn clean compile
```

### Running Tests

The project uses different suffixes to distinguish test types:

| Test Type             | Command             | File Pattern    |
|:----------------------|:--------------------|:----------------|
| **Unit Tests**        | `mvn test`          | `*Test.java`    |
| **Integration Tests** | `mvn verify`        | `*IT.java`      |
| **E2E Tests**         | `mvn verify -P e2e` | `*E2ETest.java` |

---

## 💡 Best Practices

1. **Singleton Instance**: Always reuse the `IndustriesSettingsClient`.
2. **Resource Cleanup**: Call `client.close()` during application shutdown to release HTTP and Redis resources.
3. **Logging**: Enable `DEBUG` logging for `io.github.jlmc.settings.client` to trace cache hits/misses and HTTP calls.
4. **Timeouts**: Customize timeouts for your environment:

```java
IndustriesSettingsClient client = IndustriesSettingsClient.builder()
        .connectionTimeout(Duration.ofSeconds(2))
        .requestTimeout(Duration.ofSeconds(5))
        .build();
```
