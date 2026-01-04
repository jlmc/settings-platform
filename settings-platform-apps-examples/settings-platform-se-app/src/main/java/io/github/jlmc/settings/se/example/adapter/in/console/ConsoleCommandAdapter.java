package io.github.jlmc.settings.se.example.adapter.in.console;

import io.github.jlmc.settings.domain.entities.ConfigurationType;
import io.github.jlmc.settings.se.example.application.in.GetConfigurationUseCase;
import io.github.jlmc.settings.se.example.domain.MyConfig;

import java.util.Scanner;


public class ConsoleCommandAdapter {

    private final GetConfigurationUseCase useCase;

    public ConsoleCommandAdapter(GetConfigurationUseCase useCase) {
        this.useCase = useCase;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Command:");
        System.out.println("get-config <accountId> <serviceName> <type>");
        System.out.println("exit");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                break;
            }

            if (input.startsWith("get-config")) {
                String[] parts = input.split("\\s+");
                if (parts.length != 4) {
                    System.out.println(
                            "Usage: get-config <accountId> <serviceName> <type>"
                    );
                    continue;
                }

                try {

                    String accountId = parts[1];
                    String serviceName = parts[2];
                    ConfigurationType type = ConfigurationType.valueOf(parts[3].toUpperCase());


                    MyConfig config = useCase.execute(
                            accountId,
                            serviceName,
                            type,
                            null
                    );

                    System.out.println(config);

                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid ConfigurationType");
                } catch (Exception e) {
                    System.out.println("ERROR: " + e.getMessage());
                }

            } else {
                System.out.println("Unknown command");
            }
        }
    }
}
