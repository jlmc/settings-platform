package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.entities;

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
