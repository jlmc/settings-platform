# POC TDK Settings L1/L2 Service

This project is a Proof of Concept (POC) for managing JSON schemas for different services.

## API Invocations with Curl

Below are examples of how to interact with the service using `curl`.

### 1. Define or Update Service Schemas

Creates or updates the JSON schemas for a specific service.

**Endpoint:** `PUT /schemas`

**Request Example:**

```bash
curl -X PUT http://localhost:8080/schemas \
  -H "Content-Type: application/json" \
  -d '{
    "service_name": "my-service",
    "json_schemas": [
      {
        "type": "ACCOUNT",
        "value": {
          "type": "object",
          "properties": {
            "setting1": { "type": "string" }
          },
          "required": ["setting1"]
        }
      }
    ],
    "rsa": {
      "public_key": "ssh-rsa AAAAB3Nza..."
    }
  }'
```

*Note: The API uses snake_case for property names as configured in `application.yaml`.*

### 2. Get Service Schemas

Retrieves the JSON schemas for a specified service.

**Endpoint:** `GET /schemas/{service_name}`

**Request Example:**

```bash
curl -X GET http://localhost:8080/schemas/my-service \
    -H "Accept: application/json"
    -H "Content-Type: application/json"
```

### 3. Delete Service Schemas

Deletes the JSON schemas for a specified service.

**Endpoint:** `DELETE /schemas/{service_name}`

**Request Example:**

```bash
curl -X DELETE http://localhost:8080/schemas/my-service
```