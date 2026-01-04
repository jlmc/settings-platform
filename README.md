# 🌐 Settings Platform

A comprehensive platform for managing, resolving, and caching hierarchical configurations. This project provides a central server, a Java/Kotlin SDK, and Spring Boot integration.

## 🏗️ Project Structure

The project is organized into several modules:

- **[`settings-domain`](./settings-domain)**: Core domain models, entities, and business logic for configuration resolution.
- **[`settings-service`](./settings-service)**: Spring Boot server application providing a REST API for configuration management and resolution.
- **[`settings-client`](./settings-client)**: Java/Kotlin SDK for interacting with the settings service, featuring advanced L1/L2 caching.
- **[`settings-client-spring-boot-starter`](./settings-client-spring-boot-starter)**: Spring Boot Starter for seamless integration of the SDK.
- **[`settings-webflux-app-example`](./settings-platform-apps-examples/settings-webflux-app-example)**: A sample application demonstrating the platform's usage.

## 🚀 Getting Started

### Prerequisites
- **Java 21**: The project targets Java 21 (with some modules supporting Java 17).
- **Maven**: For building the project.
- **Docker**: For running infrastructure (MongoDB, Redis, RabbitMQ).

### Infrastructure Setup

The project uses Docker Compose profiles to manage different sets of services:

- **`dev` (default)**: Starts basic infrastructure (Redis, MongoDB).
- **`apps`**: Starts the infrastructure, the application services (`settings-service`, `settings-webflux-app-example`), and an **API Gateway**.
- **`full`**: Starts all available services, including Kafka, LocalStack, and RabbitMQ.

To start the applications and their required infrastructure:

```bash
docker compose --profile apps up -d
```

Once started, the services are available through the API Gateway at `http://localhost`:
- **Settings Service**:
    - `http://localhost/settings-service/schemas/`
    - `http://localhost/settings-service/settings/`
    - `http://localhost/settings-service/configurations/`
- **WebFlux Example App**: `http://localhost/example-app/hls/{account_id}/patients/data`
- **Gateway Health**: `http://localhost/health`

For detailed `curl` examples, see **[API Usage Examples](./docs/api-examples.md)**.

Alternatively, you can access them directly:
- **Settings Service**: `http://localhost:8080`
- **WebFlux Example App**: `http://localhost:8081`

To start only the basic infrastructure:

```bash
docker compose up -d
```

### Building the Project

Build all modules from the root directory:

```bash
mvn clean install
```

## 🛠️ Core Concepts

### Hierarchical Configuration
Settings are resolved based on a hierarchy (Account > Service > Team > User > Agent), allowing for fine-grained overrides at different levels.

### L1/L2 Caching
The SDK supports a two-layer caching strategy:
- **L1 (Local)**: Fast, in-memory cache using Caffeine.
- **L2 (Distributed)**: Shared cache using Redis, with PUB/SUB for invalidating L1 caches across instances.

## 🧪 Testing

The project uses a standard testing approach:
- `mvn test`: Runs unit tests.
- `mvn verify`: Runs integration tests.
- `mvn verify -P e2e`: Runs end-to-end tests.
