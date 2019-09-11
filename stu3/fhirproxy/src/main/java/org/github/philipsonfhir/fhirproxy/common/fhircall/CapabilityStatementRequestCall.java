package org.github.philipsonfhir.fhirproxy.common.fhircall;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.github.philipsonfhir.fhirproxy.common.ImplementationGuide;
import org.github.philipsonfhir.fhirproxy.controller.service.FhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CapabilityStatementRequestCall implements FhirCall {
    private final HttpServletResponse response;
    private final static FhirContext fhirContext = FhirContext.forDstu3();
    private final HttpServletRequest request;
    private final FhirServer fhirServer;
    private final List<ImplementationGuide> implementationGuides;
    private CapabilityStatement capabilityStatement;
    private boolean done = false;

    public CapabilityStatementRequestCall(FhirServer fhirServer, HttpServletRequest request, HttpServletResponse response, List<ImplementationGuide> implementationGuides) {
        this.fhirServer = fhirServer;
        this.response = response;
        this.request  = request;
        this.implementationGuides = implementationGuides;
    }


    @Override
    public void execute() {
        capabilityStatement = fhirServer.getCapabilityStatement();

        implementationGuides.forEach(implementationGuide -> implementationGuide.updateCapabilityStatement(capabilityStatement));

        // update capabilityStatement
        PrintWriter out = null;
        String result = "";
        String contentType = request.getContentType();
        contentType = ( contentType==null? contentType="application/fhir+json":contentType);
        switch( contentType ){
            case "application/json":
            case "application/fhir+json":
                response.setContentType("application/fhir+json");
                result = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(capabilityStatement);
                break;
            case "application/xml":
            case "application/fhir+xml":
                response.setContentType("application/fhir+xml");
                result = fhirContext.newXmlParser().setPrettyPrint(true).encodeResourceToString(capabilityStatement);
                break;
        }

        try {
            out = response.getWriter();
            out.print( result );
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.OK.value());
        done = true;
    }

    @Override
    public String getStatusDescription() {
        return (done?"processing":"done");
    }

    @Override
    public IBaseResource getResource() {
        return (done?capabilityStatement:null);
    }

    @Override
    public FhirServer getFhirServer() {
        return this.fhirServer;
    }

    @Override
    public Map<String, OperationOutcome> getErrors() {
        return Collections.emptyMap();
    }

}
