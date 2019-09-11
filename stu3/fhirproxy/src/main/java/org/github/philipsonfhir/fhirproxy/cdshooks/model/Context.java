package org.github.philipsonfhir.fhirproxy.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Context {
    // patient-view
    String patientId;
    String encounterId;
}
