# 🧪 settings-webflux-app-example

A sample Spring WebFlux application demonstrating the integration and usage of the `settings-client` SDK.

## 📦 Features

- **WebFlux Integration**: Shows how to use the settings client in a reactive environment.
- **Caching**: Demonstrates L1/L2 caching configuration using Caffeine and Redis.
- **Client Usage**: Examples of fetching and using resolved configurations.

## 🚀 Getting Started

### Prerequisites
- Java 17+ (as per `pom.xml`)
- Running `settings-service`
- Redis (for caching)

### Running the Application

#### Using Maven
```bash
mvn spring-boot:run
```

The application will start on port `8081` (assuming default Spring Boot port is changed if running alongside the service).

#### Using Docker
To build the Docker image, run the following command from the **root directory** of the project:

```bash
docker build -t settings-webflux-app-example -f settings-webflux-app-example/Dockerfile .
```

To run the container:
```bash
docker run -p 8081:8081 settings-webflux-app-example
```

## 🛠️ Configuration

The application is configured in `src/main/resources/application.properties` or `application.yaml`.

Key settings:
- `settings.webflux-app.client.api-base-url`: URL of the `settings-service`.
- `settings.webflux-app.client.redis-enabled`: Enable/disable Redis caching.

## 🔍 Examples

Check the `io.github.jlmc.settings.webflux.example` package for implementation details on how the `IndustriesSettingsClient` is used to retrieve configurations.
