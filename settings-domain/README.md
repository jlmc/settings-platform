# 🏗️ settings-domain

The `settings-domain` module contains the core domain models, entities, and business components shared across the settings platform (both server and client).

## 📦 Features

- **Domain Entities**: Core models like `SettingsAccount`, `ResolvedConfiguration`, `ConfigurationType`, and `JsonSchema`.
- **Business Components**:
    - `ResolvedConfigurationAssembler`: Logic for merging and resolving configurations.
    - `ObjectNodeMerger`: Port and implementation for merging JSON objects.
    - `RSADecryptor`: Utility for handling encrypted settings.
- **Port/Adapter Pattern**: Defines interfaces (ports) that are implemented by specific technology adapters.

## 🚀 Usage

This module is intended to be a dependency for other modules in the project.

```xml
<dependency>
    <groupId>io.github.jlmc</groupId>
    <artifactId>settings-domain</artifactId>
    <version>${project.version}</version>
</dependency>
```

## 🛠️ Core Concepts

### Configuration Resolution
The module provides the logic to merge configurations from different levels into a single `ResolvedConfiguration`. 
The default resolution hierarchy is:
**Service > Account > User > Agent**

Settings at lower levels (e.g., Agent) override settings at higher levels (e.g., Service).

### Encryption
Supports RSA encryption for sensitive configuration values, ensuring they are only decrypted when needed.
