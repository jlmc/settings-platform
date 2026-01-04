# 🐳 Docker Services

This directory contains Docker Compose configurations and related resources for the platform's infrastructure.

## 🚀 Usage

### Start Specific Services
```shell
docker compose up -d mongodb redis
```

### Start Kafka Stack
```shell
docker compose up -d kafka kafka-init kafka-ui
```

### Use Profiles
The project uses Docker Compose profiles to manage different sets of services:

- **`dev` (default)**: Starts basic infrastructure (Redis, MongoDB).
- **`apps`**: Starts the infrastructure and the application services.
- **`full`**: Starts all available services.

```shell
docker compose --profile apps up -d
```
