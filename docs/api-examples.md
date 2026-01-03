# 🚀 API Usage Examples (curl)

This document provides examples of how to interact with the Settings Platform using `curl`.

All examples assume you are running the platform via Docker Compose (`--profile apps`) and accessing it through the API Gateway at `http://localhost`.

---

## 🔍 API Gateway Health

Check if the API Gateway is up and running.

```bash
curl -i http://localhost/health
```

---

## ⚙️ Settings Service

The Settings Service manages schemas, settings, and configuration resolution.

### 1. Define Service Schema
Define the structure and validation rules for your service's configurations.

```bash
curl -L -X PUT 'http://localhost/settings-service/schemas' \
-H 'Content-Type: application/json' \
-d '{
    "service_name": "my-service",
    "rsa": null,
    "json_schemas": [
        {
            "type": "ACCOUNT",
            "schema_content": {
                "$schema": "http://json-schema.org/draft-07/schema#",
                "$id": "https://example.com/config.schema.json",
                "title": "Subscription Configuration",
                "type": "object",
                "properties": {
                    "subscriptionKey": {
                        "type": "string",
                        "description": "The unique API key for the subscription",
                        "minLength": 1
                    },
                    "environment": {
                        "type": "string",
                        "description": "The target deployment environment",
                        "enum": [
                            "development",
                            "staging",
                            "production"
                        ]
                    }
                },
                "required": [
                    "subscriptionKey",
                    "environment"
                ]
            }
        },
        {
            "type": "user",
            "schema_content": {
                "$schema": "http://json-schema.org/draft-07/schema#",
                "$id": "https://example.com/config.schema.json",
                "title": "Subscription Configuration",
                "type": "object",
                "properties": {
                    "subscriptionKey": {
                        "type": "string",
                        "description": "The unique API key for the subscription",
                        "minLength": 1
                    },
                    "environment": {
                        "type": "string",
                        "description": "The target deployment environment",
                        "enum": [
                            "development",
                            "staging",
                            "production"
                        ]
                    },
                    "role": {
                        "type": "string",
                        "description": "The agent role",
                        "minLength": 1
                    }
                },
                "required": [
                    "subscriptionKey",
                    "environment"
                ]
            }
        }
    ]
}' | jq .
```

### 2. Define Settings
Set configuration values for a specific account and service.

```bash
curl -L -X PUT 'http://localhost/settings-service/settings/1/my-service/AGENT' \
-H 'Content-Type: application/json' \
-d '{
    "subscriptionKey": "22-agent",
    "environment": "production"
}' | jq .
```

### 2. Get Service Schema
Retrieve the defined schemas for a specific service.

```bash
curl -L 'http://localhost/settings-service/schemas/my-service' | jq .
```

### 3. Define Settings
Set configuration values for a specific account and service.

```bash
curl -L -X PUT 'http://localhost/settings-service/settings/1/my-service/ACCOUNT' \
-H 'Content-Type: application/json' \
-d '{
    "subscriptionKey": "22-agent",
    "environment": "production"
}' | jq .
```

### 4. Get Settings
Retrieve the specific settings for an account/service/level.

```bash
curl -L 'http://localhost/settings-service/settings/1/my-service/account' | jq .
```

### 5. Resolve Configuration
Resolve the final configuration by merging hierarchical levels (Account, Service, etc.).

```bash
curl -L 'http://localhost/settings-service/configurations/1/my-service/account' | jq .
```

---

## 🏥 WebFlux Example App

The example application demonstrates how a microservice consumes settings via the SDK.

### 1. Get Patients Data
Retrieve patient data for a specific account. This endpoint internally uses the Settings SDK to fetch configurations.

```bash
curl -L 'http://localhost/example-app/hls/1/patients/data' | jq .
```

---

## 💡 Pro-Tips

### Using jq
Pipe your `curl` output to `jq` for better readability:
```bash
curl -s http://localhost/settings-service/schemas/patient-service | jq .
```

### Verbose Output
Use `-i` to see response headers or `-v` for full request/response details:
```bash
curl -v http://localhost/health
```
