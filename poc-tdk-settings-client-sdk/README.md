# 📘 poc-tdk-settings-client-sdk

Java/Kotlin SDK for Industries Settings, with support for **OAuth2 token acquisition**, **configuration retrieval**, and **Redis caching**.  

Supports:

- Unit tests
- Integration tests
- End-to-end (E2E) tests
- Resilient HTTP client with retry
- JWT and OAuth2 Client Credentials

---

## 📁 Project Structure

```

src
├── main/java                 # Production code
├── test/java                 # Unit tests (*Test.java)
├── integration-test/java     # Integration tests (*IT.java)
│   └── resources
└── e2e-test/java             # End-to-end tests (*E2ETest.java)
└── resources
└── application-e2e.yml

````

---

## 🧪 Testing Conventions

| Test Type        | Folder                      | Naming Suffix   | Description                                    |
|------------------|-----------------------------|-----------------|------------------------------------------------|
| Unit Test        | `src/test/java`             | `*Test.java`    | Isolated tests using mocks (Mockito/AssertJ)   |
| Integration Test | `src/integration-test/java` | `*IT.java`      | Partial integration (HTTP mocks, Redis, DB)    |
| E2E Test         | `src/e2e-test/java`         | `*E2ETest.java` | Full system test with real services or staging |

---

## ⚙️ Maven Plugins

- **Surefire** → runs unit tests (`*Test.java`)  
- **Failsafe** → runs integration and E2E tests (`*IT.java` / `*E2ETest.java`)  
- **Build Helper** → adds `integration-test` and `e2e-test` folders as source sets  

---

## 🚀 Running Tests

| Command                    | Executes                       |
|----------------------------|--------------------------------|
| `mvn test`                 | Unit tests only                |
| `mvn verify`               | Unit + Integration + E2E tests |
| `mvn verify -DskipITs`     | Unit + E2E tests only          |
| `mvn verify -DskipTests`   | Integration + E2E tests only   |
| `mvn clean install -P e2e` | Full build with E2E profile    |

---

## 🔧 Dependencies

- **SLF4J / Logback** → logging  
- **Jackson** → JSON (optional, compile-only)  
- **Nimbus JOSE + JWT** → JWT signing and validation  
- **JUnit 5** → unit & integration tests  
- **Mockito** → mocking  
- **AssertJ** → fluent assertions  
- **Testcontainers** → integration & E2E tests with real infra  

---

## 🏗️ Coding & Testing Guidelines

1. **Unit Tests**
   - Isolate dependencies with Mockito
   - Verify token acquisition strategies and HTTP request building
   - No external infra

2. **Integration Tests**
   - Test with in-memory or mocked HTTP/Redis
   - Name classes `*IT.java`
   - Located in `src/integration-test/java`

3. **E2E Tests**
   - Test complete workflow: token acquisition → configuration retrieval → cache
   - Use real or staging services
   - Name classes `*E2ETest.java`
   - Located in `src/e2e-test/java`
   - Use separate `application-e2e.yml` for environment configs

4. **Logging**
   - Unit tests → debug-level logs optional
   - Integration/E2E → info-level logging recommended

5. **Profiles**
   - Use Maven profiles (`-P e2e`) for switching configurations
   - Keep credentials and URLs in separate secure property files or environment variables

---

## 📌 Notes

- **Do not commit real secrets**. Use environment variables or CI/CD secret management.
- **Retry policies** are configured via `Resilience4jRetryExecutor`.
- **Redis caching** is optional and configured in builder via `RedisBuilder`.
- All HTTP URLs and paths are built using **`UrlBuilder`** to avoid double slashes.

---

## ⚡ Quick Start Example

```java
IndustriesSettingsClient client = IndustriesSettingsClient.builder()
        .apiBaseUrl("https://api.talkdesk.com")
        .build();

ConfigurationRequest request = ConfigurationRequest.standard(
        new ClientCredentials("clientId", "secret", "https://auth.example.com/token", List.of("scope1")),
        "RoutingService",
        ConfigurationType.AGENT,
        "123"
);

String token = client.getConfiguration(request, String.class);
System.out.println("Configuration: " + token);

```
