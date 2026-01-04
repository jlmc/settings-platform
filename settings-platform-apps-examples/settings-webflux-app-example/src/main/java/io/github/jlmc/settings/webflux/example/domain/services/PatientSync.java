package io.github.jlmc.settings.webflux.example.domain.services;

import io.github.jlmc.settings.webflux.example.domain.entities.Patient;
import io.github.jlmc.settings.webflux.example.domain.repositories.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.IntStream;

@Component
public class PatientSync implements CommandLineRunner {

    private final PatientRepository patientRepository;

    public PatientSync(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public void run(String... args) {
        List<Patient> list = IntStream.rangeClosed(1, 5)
                .mapToObj(index -> new Patient(
                        "" + index,
                        "1",
                        "external-" + index,
                        "Patient " + index
                )).toList();

        patientRepository.saveAll(list).blockLast();
    }
}
