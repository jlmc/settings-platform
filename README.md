# 🌐 Settings Platform

A comprehensive platform for managing, resolving, and caching hierarchical configurations. This project provides a central server, a Java/Kotlin SDK, and Spring Boot integration.

## 🏗️ Project Structure

The project is organized into several modules:

- **[`settings-domain`](./settings-domain)**: Core domain models, entities, and business logic for configuration resolution.
- **[`settings-service`](./settings-service)**: Spring Boot server application providing a REST API for configuration management and resolution.
- **[`settings-client`](./settings-client)**: Java/Kotlin SDK for interacting with the settings service, featuring advanced L1/L2 caching.
- **[`settings-client-spring-boot-starter`](./settings-client-spring-boot-starter)**: Spring Boot Starter for seamless integration of the SDK.
- **[`settings-webflux-app-example`](./settings-webflux-app-example)**: A sample application demonstrating the platform's usage.

## 🚀 Getting Started

### Prerequisites
- **Java 21**: The project targets Java 21 (with some modules supporting Java 17).
- **Maven**: For building the project.
- **Docker**: For running infrastructure (MongoDB, Redis, RabbitMQ).

### Infrastructure Setup

Use the provided `docker-compose.yaml` to start the required infrastructure:

```bash
docker-compose up -d
```

This will start:
- **MongoDB**: Primary storage for the settings service.
- **Redis**: Shared cache and synchronization.
- **RabbitMQ**: Messaging (if used by specific components).
- **WireMock**: For mocking external dependencies in tests.

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
