package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.services;

import io.gihub.jlmc.poc.commons.settings.ConfigurationRequest;
import io.gihub.jlmc.poc.commons.settings.ConfigurationType;
import io.gihub.jlmc.poc.commons.settings.auth.BearerTokenCredentials;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.entities.PatientDataRepresentation;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.ports.IndustriesSettingsProviderPort;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.repositories.PatientRepository;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.settings.IntegrationGeneric;
import org.slf4j.Logger;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class PatientService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    private final IndustriesSettingsProviderPort industriesSettingsProviderPort;

    public PatientService(PatientRepository patientRepository, IndustriesSettingsProviderPort industriesSettingsProviderPort) {
        this.patientRepository = patientRepository;
        this.industriesSettingsProviderPort = industriesSettingsProviderPort;
    }

    public Mono<List<PatientDataRepresentation>> find(String accountId) {
        LOGGER.info("Finding patients data for account id: {}", accountId);

        return fetchSettings(accountId)
                .flatMap(settings -> findPatientsWithSettings(accountId, settings));
    }

    @NonNull
    public Mono<List<PatientDataRepresentation>> findPatientsWithSettings(String accountId, IntegrationGeneric integrationGeneric) {
        Mono<List<PatientDataRepresentation>> result =
                patientRepository.findByAccountId(accountId)
                        .doOnSubscribe(subscription -> LOGGER.info("Subscribed to patient repository for accountId={}", accountId))
                        .doOnNext(patient -> LOGGER.info("Retrieved patient: id={}, name={}", patient.id(), patient.name()))
                        .map(it -> {
                            PatientDataRepresentation dto = new PatientDataRepresentation(
                                    it.id(),
                                    it.name(),
                                    "%s/%s".formatted(it.accountId(), it.externalId()),
                                    integrationGeneric.subscriptionKey() + " <---> " + integrationGeneric.environment()
                            );

                            LOGGER.debug("Mapped patient to DTO: {}", dto);

                            return dto;
                        })
                        .doOnComplete(() -> LOGGER.info("Completed fetching patients for accountId={}", accountId))
                        .doOnError(e -> LOGGER.error("Error fetching patients for accountId={}", accountId, e))

                        .collectList()
                        .doOnSuccess(list -> LOGGER.info("Returning {} patient(s) for accountId={}", list.size(), accountId));
        return result;
    }

    public Mono<IntegrationGeneric> fetchSettings(String accountId) {
        ConfigurationRequest settingsRequest =
                ConfigurationRequest.standardWithAccountId(
                        new BearerTokenCredentials("token-for-%s".formatted(accountId)),
                        "my-service",
                        ConfigurationType.ACCOUNT,
                        accountId,
                        accountId
                );

        return industriesSettingsProviderPort
                .getSettings(settingsRequest, IntegrationGeneric.class)
                .doOnSuccess(settings ->
                        LOGGER.debug("Settings fetched successfully for accountId={}", accountId)
                )
                .doOnError(e ->
                        LOGGER.error("Failed to fetch settings for accountId={}", accountId, e)
                );
    }

}
