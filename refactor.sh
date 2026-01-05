#!/bin/bash

# Define project path
BASE_DIR="settings-client/src"

# Function to replace text in files
replace_in_files() {
    local search=$1
    local replace=$2
    find "$BASE_DIR" -type f -name "*.java" -print0 | xargs -0 sed -i '' "s|$search|$replace|g"
}

# Function to add missing imports
add_import() {
    local class_name=$1
    local package_name=$2
    find "$BASE_DIR" -type f -name "*.java" -print0 | while read -d $'\0' file; do
        if grep -q "$class_name" "$file" && ! grep -q "package $package_name" "$file" && ! grep -q "import $package_name.$class_name" "$file"; then
            sed -i '' "/^package /a\\
import $package_name.$class_name;" "$file"
        fi
    done
}

echo "Updating package declarations..."

# Core
find "$BASE_DIR/main/java/io/github/jlmc/settings/client/core" -maxdepth 1 -type f -name "*.java" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client;|package io.github.jlmc.settings.client.core;|g"
find "$BASE_DIR/test/java/io/github/jlmc/settings/client/core" -maxdepth 1 -type f -name "*.java" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client;|package io.github.jlmc.settings.client.core;|g"
find "$BASE_DIR" -type f -name "*.java" -path "*/auth/*" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client.auth;|package io.github.jlmc.settings.client.core.auth;|g"
find "$BASE_DIR" -type f -name "*.java" -path "*/exceptions/*" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client.exceptions;|package io.github.jlmc.settings.client.core.exceptions;|g"

# Ports Out
sed -i '' "s|package io.github.jlmc.settings.client.json;|package io.github.jlmc.settings.client.ports.out;|g" "$BASE_DIR/main/java/io/github/jlmc/settings/client/ports/out/JsonDeserializer.java"
sed -i '' "s|package io.github.jlmc.settings.client.http;|package io.github.jlmc.settings.client.ports.out;|g" "$BASE_DIR/main/java/io/github/jlmc/settings/client/ports/out/HttpExecutionStrategy.java"
sed -i '' "s|package io.github.jlmc.settings.client.token;|package io.github.jlmc.settings.client.ports.out;|g" "$BASE_DIR/main/java/io/github/jlmc/settings/client/ports/out/AccessTokenProvider.java"
sed -i '' "s|package io.github.jlmc.settings.client.redis;|package io.github.jlmc.settings.client.ports.out;|g" "$BASE_DIR/main/java/io/github/jlmc/settings/client/ports/out/DistributedConfigProvider.java"
sed -i '' "s|package io.github.jlmc.settings.client.resilience;|package io.github.jlmc.settings.client.ports.out;|g" "$BASE_DIR/main/java/io/github/jlmc/settings/client/ports/out/RetryExecutor.java"

# Adapters
find "$BASE_DIR" -type f -name "*.java" -path "*/adapters/http/*" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client.http;|package io.github.jlmc.settings.client.adapters.http;|g"
find "$BASE_DIR" -type f -name "*.java" -path "*/adapters/redis/*" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client.redis;|package io.github.jlmc.settings.client.adapters.redis;|g"
find "$BASE_DIR" -type f -name "*.java" -path "*/adapters/json/*" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client.json;|package io.github.jlmc.settings.client.adapters.json;|g"
find "$BASE_DIR" -type f -name "*.java" -path "*/adapters/token/*" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client.token;|package io.github.jlmc.settings.client.adapters.token;|g"
find "$BASE_DIR" -type f -name "*.java" -path "*/adapters/resilience/*" -print0 | xargs -0 sed -i '' "s|package io.github.jlmc.settings.client.resilience;|package io.github.jlmc.settings.client.adapters.resilience;|g"

echo "Updating imports and references..."

# Ports Out
add_import "JsonDeserializer" "io.github.jlmc.settings.client.ports.out"
add_import "HttpExecutionStrategy" "io.github.jlmc.settings.client.ports.out"
add_import "AccessTokenProvider" "io.github.jlmc.settings.client.ports.out"
add_import "DistributedConfigProvider" "io.github.jlmc.settings.client.ports.out"
add_import "RetryExecutor" "io.github.jlmc.settings.client.ports.out"
add_import "ClientHttpRequest" "io.github.jlmc.settings.client.core"

# Order matters to avoid partial replacements
replace_in_files "io.github.jlmc.settings.client.auth" "io.github.jlmc.settings.client.core.auth"
replace_in_files "io.github.jlmc.settings.client.exceptions" "io.github.jlmc.settings.client.core.exceptions"

# Before replacing .http, .redis etc, we must handle the ports vs adapters.
# This is tricky because the original code used the same package for interface and implementation.
# Now we separated them.

# Let's map specific classes to their new packages if they are ports.
# Interfaces (Ports Out)
replace_in_files "io.github.jlmc.settings.client.json.JsonDeserializer" "io.github.jlmc.settings.client.ports.out.JsonDeserializer"
replace_in_files "io.github.jlmc.settings.client.http.HttpExecutionStrategy" "io.github.jlmc.settings.client.ports.out.HttpExecutionStrategy"
replace_in_files "io.github.jlmc.settings.client.token.AccessTokenProvider" "io.github.jlmc.settings.client.ports.out.AccessTokenProvider"
replace_in_files "io.github.jlmc.settings.client.redis.DistributedConfigProvider" "io.github.jlmc.settings.client.ports.out.DistributedConfigProvider"
replace_in_files "io.github.jlmc.settings.client.resilience.RetryExecutor" "io.github.jlmc.settings.client.ports.out.RetryExecutor"

# Rest are adapters
replace_in_files "io.github.jlmc.settings.client.http" "io.github.jlmc.settings.client.adapters.http"
replace_in_files "io.github.jlmc.settings.client.redis" "io.github.jlmc.settings.client.adapters.redis"
replace_in_files "io.github.jlmc.settings.client.json" "io.github.jlmc.settings.client.adapters.json"
replace_in_files "io.github.jlmc.settings.client.token" "io.github.jlmc.settings.client.adapters.token"
replace_in_files "io.github.jlmc.settings.client.resilience" "io.github.jlmc.settings.client.adapters.resilience"

# Core classes
replace_in_files "io.github.jlmc.settings.client.IndustriesSettingsClient" "io.github.jlmc.settings.client.core.IndustriesSettingsClient"
replace_in_files "io.github.jlmc.settings.client.Builder" "io.github.jlmc.settings.client.core.Builder"
replace_in_files "io.github.jlmc.settings.client.ConfigurationRequest" "io.github.jlmc.settings.client.core.ConfigurationRequest"
replace_in_files "io.github.jlmc.settings.client.LibraryDetector" "io.github.jlmc.settings.client.core.LibraryDetector"

echo "Done."
