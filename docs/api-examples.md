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
curl -X PUT http://localhost/schemas \
  -H "Content-Type: application/json" \
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
}}'
```

### 2. Get Service Schema
Retrieve the defined schemas for a specific service.

```bash
curl -i http://localhost/schemas/patient-service
```

### 3. Define Settings
Set configuration values for a specific account and service.

```bash
curl -X PUT http://localhost/settings/ACC123/patient-service/ACCOUNT \
  -H "Content-Type: application/json" \
  -d '{
    "featureEnabled": true,
    "maxRetries": 5
  }'
```

### 4. Get Settings
Retrieve the specific settings for an account/service/level.

```bash
curl -i http://localhost/settings/ACC123/patient-service/ACCOUNT
```

### 5. Resolve Configuration
Resolve the final configuration by merging hierarchical levels (Account, Service, etc.).

```bash
curl -i http://localhost/configurations/ACC123/patient-service/ACCOUNT
```

---

## 🏥 WebFlux Example App

The example application demonstrates how a microservice consumes settings via the SDK.

### 1. Get Patients Data
Retrieve patient data for a specific account. This endpoint internally uses the Settings SDK to fetch configurations.

```bash
curl -i http://localhost/hls/ACC123/patients/data
```

---

## 💡 Pro-Tips

### Using jq
Pipe your `curl` output to `jq` for better readability:
```bash
curl -s http://localhost/schemas/patient-service | jq .
```

### Verbose Output
Use `-i` to see response headers or `-v` for full request/response details:
```bash
curl -v http://localhost/health
```
