package com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.fhir;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;
import java.util.logging.Logger;

public class FhirServerBulkdata implements IFhirServer {

    private final IFhirServer fhirServer;
    private final Logger logger = Logger.getLogger( FhirServerBulkdata.class.getName() );
    private final PatientExportServer fhirPatientExportServer;

    public FhirServerBulkdata(IFhirServer iFhirServer){
        this.fhirServer = iFhirServer;
        this.fhirPatientExportServer = new PatientExportServer( this.fhirServer );
    }

    @Override
    public CapabilityStatement getCapabilityStatement() {
        return this.fhirServer.getCapabilityStatement();
    }

    @Override
    public IBaseResource searchResource(String resourceType, Map<String, String> queryParams) {
        if ( resourceType.equals( "$export" ) ) {
            String outputFormat = queryParams.get( "outputFormat" );
            String since = queryParams.get( "since" );
            String type = queryParams.get( "type" );
            return this.fhirPatientExportServer.exportAllData( type );
        }
        return this.fhirServer.searchResource(resourceType, queryParams);
    }

    @Override
    public IBaseResource getResource(String resourceType, String id, Map<String, String> queryParams) throws FHIRException {
        if ( resourceType.equals("Patient") && id.equals("$export")){
            logger.info("Patient - $export");
            String outputFormat = queryParams.get( "outputFormat" );
            String since = queryParams.get( "since" );
            String type = queryParams.get( "type" );
            return this.fhirPatientExportServer.exportAllPatientData( outputFormat, since, type );
        }
        return this.fhirServer.getResource(resourceType, id, queryParams );
    }

    @Override
    public IBaseResource getResourceOperation(String resourceType, String operationName, Map<String, String> queryParams) throws FHIRException {
        if ( resourceType.equals( "Patient" ) && operationName.equals( "$export" ) ) {
            logger.info( "Patient - $export" );
            String outputFormat = queryParams.get( "outputFormat" );
            String since = queryParams.get( "since" );
            String type = queryParams.get( "type" );
            return this.fhirPatientExportServer.exportAllPatientData( outputFormat, since, type );
        }
        return this.fhirServer.getResource( resourceType, operationName, queryParams );
    }

    @Override
    public IBaseResource getResource(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException {
        if ( resourceType.equals("Patient") && params.equals("$export")){
            logger.info("Patient - "+ id+" - $export");
            String outputFormat = queryParams.get("outputFormat");
            String since        = queryParams.get("since");
            String type         = queryParams.get("type");
            return this.fhirPatientExportServer.exportPatientData( id, outputFormat, since, type );
        }
        if ( resourceType.equals( "Group" ) && params.equals( "$export" ) ) {
            logger.info( "Group - " + id + " - $export" );
            String outputFormat = queryParams.get( "outputFormat" );
            String since = queryParams.get( "since" );
            String type = queryParams.get( "type" );
            return this.fhirPatientExportServer.exportAllGroupData( id, outputFormat, since, type );
        }
        return this.fhirServer.getResource(resourceType, id, params, queryParams );
    }

    @Override
    public Bundle loadPage(Bundle resultBundle) throws FHIRException {
        return this.fhirServer.loadPage(  resultBundle );
    }

    @Override
    public IBaseOperationOutcome putResource(IBaseResource iBaseResource) throws FHIRException {
        return this.fhirServer.putResource( iBaseResource );
    }

    @Override
    public IBaseOperationOutcome postResource(IBaseResource iBaseResource) throws FHIRException {
        return this.fhirServer.postResource( iBaseResource );
    }

    @Override
    public IBaseResource postResource(String resourceType, String id, IBaseResource parseResource, String params, Map<String, String> queryParams) throws FHIRException {
        return this.fhirServer.postResource( resourceType, id, parseResource, params, queryParams );
    }

    @Override
    public FhirContext getCtx() {
        return this.fhirServer.getCtx();
    }
}
