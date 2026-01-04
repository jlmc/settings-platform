package io.github.jlmc.settings.webflux.example.domain.repositories;

import io.github.jlmc.settings.webflux.example.domain.entities.Patient;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PatientRepository extends ReactiveMongoRepository<Patient, String> {

    Flux<Patient> findByAccountId(String accountId);
}
