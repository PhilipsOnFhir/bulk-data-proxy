package org.github.philipsonfhir.fhirproxy.bulkdata;

import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirCall;
import org.github.philipsonfhir.fhirproxy.common.fhircall.FhirRequest;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirBaseOperation;
import org.github.philipsonfhir.fhirproxy.common.operation.FhirResourceInstanceOperation;
import org.github.philipsonfhir.fhirproxy.controller.service.FhirServer;
import org.hl7.fhir.dstu3.model.OperationDefinition;

public class PatientInstanceExportFhirOperation implements FhirResourceInstanceOperation {

    @Override
    public FhirCall createFhirCall(FhirServer fhirServer, FhirRequest fhirRequest) throws FhirProxyException {
        BulkdataParameters bulkdataParameters = new BulkdataParameters( fhirRequest );
        return new PatientInstanceExportFhirCall( fhirServer, fhirRequest.getResourceId(), bulkdataParameters.getOutputFormat(), bulkdataParameters.getSince(), bulkdataParameters.getType());
    }

    @Override
    public String getOperationName() {
        return "$export";
    }

    @Override
    public OperationDefinition getOperation() {
        return null;
    }

    @Override
    public String getResourceType() {
        return "Patient";
    }
}
