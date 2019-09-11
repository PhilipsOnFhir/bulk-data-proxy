package org.github.philipsonfhir.fhirproxy.common.fhircall;

import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyNotImplementedException;
import org.github.philipsonfhir.fhirproxy.controller.service.FhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.dstu3.model.OperationOutcome;

import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetFhirCall implements FhirCall {
    private final FhirServer fhirServer;
    private final FhirRequest fhirRequest;
    private IBaseResource result = null;
    private Map<String, OperationOutcome> errorMap = new HashMap<>();

    public GetFhirCall(FhirServer fhirServer, FhirRequest fhirRequest) {
        this.fhirServer  = fhirServer;
        this.fhirRequest = fhirRequest;
    }

    @Override
    public void execute() {
        List<String> partList = fhirRequest.getPartList();
        switch( partList.size() ){
            case 3:
                 result = fhirServer.doGet(partList.get(0), partList.get(1), partList.get(2), fhirRequest.getQueryMap());
                break;
            case 2:
                result = fhirServer.doGet( partList.get(0), partList.get(1),  fhirRequest.getQueryMap());
                break;
            case 1:
                result =fhirServer.doGet( partList.get(0), fhirRequest.getQueryMap());
                break;
            default:
                errorMap.put(fhirRequest.getResourceType(), new OperationOutcome()
                        .addIssue( new OperationOutcome.OperationOutcomeIssueComponent()
                                .setSeverity( OperationOutcome.IssueSeverity.ERROR)
                                .setDiagnostics( "GET with "+partList.size()+" parameters is not supported")
                        )
                );
                break;
        }

    }

    @Override
    public String getStatusDescription() {
        return (this.result!=null?"Done":"Processing");
    }

    @Override
    public IBaseResource getResource() {
        return result;
    }

    @Override
    public FhirServer getFhirServer() {
        return this.fhirServer;
    }

    @Override
    public Map<String, OperationOutcome> getErrors() {
        return Collections.unmodifiableMap(this.errorMap);
    }

}
