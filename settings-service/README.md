# ⚙️ settings-service

The `settings-service` is a Spring Boot application that provides a RESTful API for managing and resolving configurations. It serves as the central server for the settings platform.

## 📦 Features

- **REST API**: Endpoints for CRUD operations on settings and schemas.
- **Configuration Resolution**: Merges settings from various levels (Account, Service, etc.) based on requests.
- **Schema Validation**: Uses JSON Schema to validate configuration structures.
- **Persistence**: Powered by MongoDB.
- **Shared Cache Synchronization**: Supports Redis for synchronizing configuration updates across multiple service instances.
- **Monitoring**: Includes Spring Boot Actuator for health checks and metrics.

## 🚀 Getting Started

### Prerequisites
- Java 21
- MongoDB
- Redis (optional, for shared cache synchronization)

### Running the Application

```bash
mvn spring-boot:run
```

By default, the service starts on port `8080`.

### Configuration

Key configurations in `application.yaml`:
- `spring.mongodb.uri`: MongoDB connection string.
- `spring.data.redis.host`: Redis host.
- `tdk.shared-cache.type`: Type of shared cache synchronization (`REDIS` or `NOOP`).

## 🛣️ API Endpoints

### Service Schemas
- `PUT /schemas`: Define or update service schemas.
- `GET /schemas/{service_name}`: Retrieve service schemas.
- `DELETE /schemas/{service_name}`: Delete service schemas.

### Settings Accounts
- `PUT /settings-accounts`: Define or update settings for an account.
- `GET /settings-accounts/{accountId}`: Retrieve settings for an account.

### Configuration Resolution
- `GET /configurations`: Resolve configurations.

## 🏗️ Architecture

The service follows a hexagonal architecture (Ports and Adapters):
- **Domain**: Core logic and entities (reusing `settings-domain`).
- **Adapters**:
    - `http`: REST controllers and mappers.
    - `mongo`: MongoDB repositories.
    - `sharedcache`: Redis-based synchronization logic.
