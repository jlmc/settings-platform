package io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.adapters;

import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.entities.PatientDataRepresentation;
import io.github.jlmc.poc.tdk.settings.l1l2.appwebflux.domain.services.PatientService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/hls/{account_id}/patients/data")
public class PatientsDataController {

    public final PatientService patientService;

    public PatientsDataController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Mono<List<PatientDataRepresentation>> find(@PathVariable("account_id") String accountId) {
        return patientService.find(accountId);
    }
}
