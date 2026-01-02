package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.repositories;

import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.entities.Patient;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PatientRepository extends ReactiveMongoRepository<Patient, String> {

    Flux<Patient> findByAccountId(String accountId);
}
