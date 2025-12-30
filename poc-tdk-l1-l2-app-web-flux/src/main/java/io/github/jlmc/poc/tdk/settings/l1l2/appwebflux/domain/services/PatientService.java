package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.services;

import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.entities.PatientDataRepresentation;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.repositories.PatientRepository;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;


@Service
public class PatientService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Mono<List<PatientDataRepresentation>> find(String accountId) {
        LOGGER.info("Finding patients data for account id: {}", accountId);

        Mono<List<PatientDataRepresentation>> result =
                patientRepository.findByAccountId(accountId)
                        .doOnSubscribe(subscription -> LOGGER.info("Subscribed to patient repository for accountId={}", accountId))
                        .doOnNext(patient -> LOGGER.info("Retrieved patient: id={}, name={}", patient.id(), patient.name()))
                        .map(it -> {
                            PatientDataRepresentation dto = new PatientDataRepresentation(
                                    it.id(),
                                    it.name(),
                                    "%s/%s".formatted(it.accountId(), it.externalId()),
                                    null
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
}
