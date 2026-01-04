# 📘 settings-client-sdk

Java/Kotlin SDK for Industries Settings, with support for **OAuth2 token acquisition**, **configuration retrieval**, and **Redis caching**.

---

## 🚀 Quick Start

### 1. Add Dependencies

Add the SDK to your `pom.xml`. Note that most dependencies are marked as `provided` or `optional` to keep your deployment lean.

```xml
<dependency>
    <groupId>io.github.jlmc</groupId>
    <artifactId>settings-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Required for OAuth2/JWT -->
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>10.5</version>
</dependency>
```

### 2. Initialize the Client

The client is thread-safe and should be reused across your application.

```java
IndustriesSettingsClient client = IndustriesSettingsClient.builder()
        .apiBaseUrl("https://api.company.com")
        .build();
```

### 3. Fetch a Configuration

```java
AuthCredentials auth = new ClientCredentials("clientId", "secret", "https://auth.example.com");

ConfigurationRequest request = ConfigurationRequest.standard(
        auth,
        "RoutingService",
        ConfigurationType.AGENT,
        "agent-123"
);

MyConfig config = client.getConfiguration(request, MyConfig.class);
```

---

## 🏗️ Core Concepts

### Authentication Strategies
- `ClientCredentials`: Standard OAuth2 client credentials flow.
- `PrivilegedCredentials`: RSA-signed JWT flow for internal services.
- `BearerToken`: Use a pre-existing token.

### Configuration Types
Supported types (from `ConfigurationType` enum):
- `ACCOUNT`, `SERVICE`, `TEAM`, `USER`, `AGENT`.

---

## 🔧 Dependencies

The SDK is designed to be lightweight. You only need to include what you use.

| Dependency           | Purpose                  | Scope      | Required           |
|----------------------|--------------------------|------------|--------------------|
| `setting-domain`     | Domain models            | `compile`  | ✅                  |
| `jackson-databind`   | JSON Deserialization     | `provided` | Optional (Default) |
| `slf4j-api`          | Logging                  | `provided` | ✅                  |
| `nimbus-jose-jwt`    | Token signing/validation | `provided` | Optional           |
| `lettuce-core`       | Redis support            | `provided` | Optional           |
| `caffeine`           | L1 Cache support         | `provided` | Optional           |
| `resilience4j-retry` | Retry support            | `provided` | Optional           |

---

## ⚡ Advanced Features

### RSA Encryption & Decryption
If your configurations are encrypted, provide the RSA private key in the request:

```java
ConfigurationRequest request = ConfigurationRequest.withRsaEncryption(
        auth, "ServiceX", ConfigurationType.AGENT, "id", rsaKey, interactionId
);
```

### Redis L1/L2 Caching
Enable high-performance caching to reduce API calls:

```java
// Configure Redis Provider
RedisClient redisClient = RedisClient.create("redis://localhost:6379");
DistributedConfigProvider redisProvider = new RedisDistributedConfigProvider(
    new RedisL1L2Caffeine(redisClient, "my-app", 10, TimeUnit.MINUTES, 500),
    new StandardKeyBuilder(),
    Builder.defaultJsonDeserializer(),
    new DefaultResolvedConfigurationAssembler()
);

IndustriesSettingsClient client = IndustriesSettingsClient.builder()
        .apiBaseUrl("https://api.company.com")
        .redisExecutionStrategy(redisProvider)
        .build();
```

---

## 💡 Tips & Best Practices

1.  **Singleton Client**: Create one `IndustriesSettingsClient` instance and share it. It manages its own connection pool and caches.
2.  **Resource Management**: Always call `client.close()` when your application shuts down to release HTTP and Redis connections.
3.  **Timeouts**: Default timeouts are conservative. Customize them in the builder for your specific latency requirements:
    ```java
    .connectionTimeout(Duration.ofSeconds(2))
    .requestTimeout(Duration.ofSeconds(5))
    ```
4.  **Logging**: The SDK uses SLF4J. Enable `DEBUG` logging for `io.github.jlmc.poc.commons.settings` to troubleshoot request/cache issues.
5.  **Redis L1 Cache**: Use `RedisL1L2Caffeine` for production. It uses Redis PUB/SUB to invalidate local memory caches, ensuring consistency while maintaining microsecond-level latency for hot keys.

---

## 🧪 Testing

The project distinguishes between three types of tests:

| Command             | Executes          | Suffix          |
|---------------------|-------------------|-----------------|
| `mvn test`          | Unit tests        | `*Test.java`    |
| `mvn verify`        | Integration tests | `*IT.java`      |
| `mvn verify -P e2e` | End-to-End tests  | `*E2ETest.java` |
