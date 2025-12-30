package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PatientRepository extends ReactiveMongoRepository<Patient, String> {
}
