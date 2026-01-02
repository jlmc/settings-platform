package io.github.jlmc.settings.webflux.example.domain.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "patients")
public record Patient(
        @Id
        String id,
        String accountId,
        String externalId,
        String name) {
}
