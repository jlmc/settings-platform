package io.github.jlmc.settings.se.example;

import io.github.jlmc.settings.se.example.adapter.in.console.ConsoleCommandAdapter;
import io.github.jlmc.settings.se.example.application.in.GetConfigurationUseCase;
import io.github.jlmc.settings.se.example.infrastructure.config.ApplicationConfig;

/// A simple Java SE application that demonstrates how to use the Settings Platform
/// to retrieve configuration settings for a service.
///
/// It connects to a Redis instance and the Settings Platform API
/// to fetch and display configuration data.
///
/// ## How to run:
/// 1. Ensure you have a running Redis instance with the appropriate data.
/// 2. Update the `redisUri` variable with your Redis connection details.
/// 3. Update the `apiBaseUrl` variable with the Settings Platform API URL.
/// 4. Run the application and use the console to execute commands.
/// 5. Use the command `get-config <accountId> <serviceName> <type>` to fetch configurations.
/// Example:
/// ```
/// > get-config 1 my-service ACCOUNT
/// MyConfig{name='...', value='...'}
/// ```
/// Type `exit` to terminate the application.
public class SettingsSeExampleApplication {

    public static void main(String[] args) {

        String namespace = "settings";
        String redisUri = "redis://:YOUR_STRONG_PASSWORD@localhost:6379";
        String apiBaseUrl = "http://localhost:8080";

        GetConfigurationUseCase useCase =
                ApplicationConfig.getConfigurationUseCase(namespace, redisUri, apiBaseUrl);

        new ConsoleCommandAdapter(useCase).start();
    }
}
