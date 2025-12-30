package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain;

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
    public void run(String... args) throws Exception {
        List<Patient> list = IntStream.of(1, 5)
                .mapToObj(index -> new Patient(
                        "" + index,
                        "1",
                        "external-" + index,
                        "Patient " + index
                )).toList();

        patientRepository.saveAll(list).blockLast();
    }
}
