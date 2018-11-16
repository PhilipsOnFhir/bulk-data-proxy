package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.async.AsyncService;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.operations.ExportAllFhirOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.operations.ExportAllFhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.operations.GroupExportOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.operations.PatientExportOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.service.FhirServerService;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RestController
@CrossOrigin(origins = "*", allowedHeaders = {BulkDataFhirController.CONTENT_LOCATION}, exposedHeaders = {BulkDataFhirController.CONTENT_LOCATION})
public class BulkDataFhirController {
    public static final String CONTENT_LOCATION = "Content-Location";
    private Logger logger = Logger.getLogger( this.getClass().getName());
    private FhirContext myContext = FhirContext.forDstu3();
    private static final String url = "http://localhost:9500/baseDstu3";

    FhirServer fhirServer = null;

    @Autowired
    AsyncService asyncService;
    private int port;

    public BulkDataFhirController(){
//        fhirServer = new FhirServer( "http:localhost:9500/baseStu3" );
        setFhirServerUrl("http:localhost:9500/baseStu3");
//        fhirServer = new FhirServer( "http:localhost:9500/baseStu3" );
//        String port = environment.getProperty("local.server.port");
    }

    public BulkDataFhirController( String url ){
//        fhirServer = new FhirServer( "http:localhost:9500/baseStu3" );
        setFhirServerUrl(url);
//        fhirServer = new FhirServer( url );
//        String port = environment.getProperty("local.server.port");
    }


    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}",
            produces =  "application/fhir+json"
    )
    public ResponseEntity<String> searchResourcesJson(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Prefer", defaultValue = "") String prefer,
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @PathVariable String resourceType,
            @RequestParam Map<String, String> queryParams
    ) throws NotImplementedException {
        return getStringResponseEntity( request, response, prefer, accept, resourceType, queryParams );
    }


    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}",
            produces =  "application/fhir+xml"
    )
    public ResponseEntity<String> searchResourcesXml(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Prefer", defaultValue = "") String prefer,
            @RequestHeader("Accept") String accept,
            @PathVariable String resourceType,
            @RequestParam Map<String, String> queryParams
    ) throws NotImplementedException {
        try {
            int port = new URI( request.getRequestURL().toString()).getPort();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return getStringResponseEntity( request, response, prefer, accept, resourceType, queryParams );
    }

    private ResponseEntity<String> getStringResponseEntity(HttpServletRequest request, HttpServletResponse response, @RequestHeader(value = "Prefer", defaultValue = "") String prefer, @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept, @PathVariable String resourceType, @RequestParam Map<String, String> queryParams) throws NotImplementedException {
        if ( prefer!=null && prefer.equals("respond-async") ){
            return new ResponseEntity<>(
                parser( accept ).encodeResourceToString( doAsyncBase( request, response, resourceType, queryParams ) ),
                HttpStatus.ACCEPTED
            );
        } else {
            if (resourceType.equals("metadata")) {
                logger.log( Level.INFO, "JSON GET CapabilityStatement");
                return new ResponseEntity<>(
                    parser(accept).encodeResourceToString(fhirServer.getCapabilityStatement()),
                    HttpStatus.OK
                );
            } else {
                logger.log(Level.INFO, "JSON GET " + resourceType);
                return new ResponseEntity<>(
                    parser(accept).encodeResourceToString(fhirServer.doSearch(resourceType, queryParams)),
                    HttpStatus.OK
                );
            }
        }
    }


    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+json"
    )
    public ResponseEntity getJsonResource(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Prefer", defaultValue = "") String prefer,
            @RequestHeader("Accept") String accept,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return getResource( request, response, prefer, accept, resourceType, id, queryParams );
    }


    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+xml"
    )
    public ResponseEntity getXmlResource(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Prefer", defaultValue = "") String prefer,
            @RequestHeader("Accept") String accept,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return getResource( request, response, prefer, accept, resourceType, id, queryParams );
    }

    private ResponseEntity<String> getResource(
            HttpServletRequest request,
            HttpServletResponse response,
            String prefer,
            String accept,
            String resourceType,
            String id,
            Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"GET "+resourceType+" "+id );
        if ( prefer!=null && prefer.equals("respond-async") ){
            return new ResponseEntity<>(
                parser( accept ).encodeResourceToString(
                    doAsync( request, response, resourceType, id, null, queryParams ) ),
                HttpStatus.ACCEPTED
            );
        } else {
            return new ResponseEntity<>(
                    parser(accept).encodeResourceToString(fhirServer.doGet(resourceType, id, queryParams)),
                    HttpStatus.OK
            );
        }
    }
    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+xml"
    )
    public ResponseEntity<String> getXmlResourceWithParams(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Prefer", defaultValue = "") String prefer,
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
//            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        if ( prefer!=null && prefer.equals("respond-async") ){
            return new ResponseEntity<>(
                    parser(accept).encodeResourceToString( doAsync(request, response, resourceType, id, params, queryParams)),
                    HttpStatus.ACCEPTED
            );
        } else {
            return new ResponseEntity<>(
                    parser(accept).encodeResourceToString(fhirServer.doGet(resourceType, id, params, queryParams)),
                    HttpStatus.OK
            );
        }
    }

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+json"
    )
    public ResponseEntity<String> getJsonResourceWithParams(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Prefer", defaultValue = "") String prefer,
            @RequestHeader("Accept") String accept,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        if ( prefer!=null && prefer.equals("respond-async") ){
            Map<String, String> headers = new HashMap<>();
//            return  doAsync2( request, response, accept, resourceType, id, params, queryParams  );
            return new ResponseEntity<>(
                    parser(accept).encodeResourceToString( doAsync(request, response, resourceType, id, params, queryParams)),
                    HttpStatus.ACCEPTED
            );
        } else {
            return new ResponseEntity<>(
                    parser(accept).encodeResourceToString(fhirServer.doGet(resourceType, id, params, queryParams)),
                    HttpStatus.OK
            );
        }
    }


    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.PUT,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+json"
    )
    public String putResourceJson(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return putResource( contentType, requestBody, resourceType, id, queryParams);
    }

    @RequestMapping (
            method = RequestMethod.PUT,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+xml"
    )
    public String putResourceXml(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return putResource( contentType, requestBody, resourceType, id, queryParams);
    }

    private String putResource(
            String contentType,
            String requestBody,
            String resourceType,
            String id,
            Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"PUT "+resourceType+" "+id );
        IBaseResource iBaseResource = parser( contentType ).parseResource(requestBody);
        IBaseOperationOutcome operationalOutcome = fhirServer.updateResource(iBaseResource);
        return (operationalOutcome==null?"":parser( contentType).encodeResourceToString( operationalOutcome ));
    }

    private IParser parser(String contentType) {
        if ( contentType.contains("application/fhir+xml")){
            return myContext.newXmlParser();
        } else if ( contentType.contains("application/fhir+json")){
            return myContext.newJsonParser();
        } else {
            return myContext.newJsonParser();
        }

    }

    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+xml"
    )
    public String postResourceXml(
            @RequestHeader("Accept") String accept,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept ).encodeResourceToString(fhirServer.doPost(resourceType, id, parser(contentType).parseResource(requestBody), queryParams));
    }

    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+json"
    )
    public String postResourceJson(
            @RequestHeader("Accept") String accept,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept ).encodeResourceToString(fhirServer.doPost(resourceType, id, parser(contentType).parseResource(requestBody), queryParams));
    }

    /////////////////////////////////////////////////////////////////////////////


    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+json"
    )
    public String postJsonResourceWithParams(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept )
                .encodeResourceToString(
                        fhirServer.doPost( resourceType, id, parser(contentType).parseResource(requestBody),params, queryParams )
                );
    }

    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+xml"
    )
    public String postXmlResourceWithParams(
            @RequestHeader("Accept") String accept,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept )
                .encodeResourceToString(
                        fhirServer.doPost( resourceType, id, parser(contentType).parseResource(requestBody),params, queryParams )
                );
    }

    private OperationOutcome doAsyncBase(HttpServletRequest request, HttpServletResponse response, String firstParam, Map<String, String> queryParams) {
        logger.info( "Async call operation object" );
        String sessionId = "unknown";
        if ( firstParam.equals( "$export" ) ) {
            ExportAllFhirOperation exportAllFhirOperation = new ExportAllFhirOperation( "$export" );
            ExportAllFhirOperationCall exportAllFhirOperationCall = exportAllFhirOperation.createOperationCall( fhirServer, queryParams );
            sessionId = asyncService.newAyncGetSession( request.getRequestURL().toString(), exportAllFhirOperationCall );
        } else {
            sessionId = asyncService.newAyncGetSession( request.getRequestURL().toString(), fhirServer, firstParam, null, null, queryParams );
        }

        String callUrl = request.getRequestURL().toString();

        logger.info( "Ansync request detected" );
        String sessionUrl = callUrl.substring( 0, callUrl.indexOf( "fhir/" ) ) + "async-services/" + sessionId;

        response.setStatus( 202 );
        response.setHeader( CONTENT_LOCATION, sessionUrl );
        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setSeverity( OperationOutcome.IssueSeverity.INFORMATION )
            .setCode( OperationOutcome.IssueType.VALUE )
            .addLocation( sessionUrl )
        ;
        return operationOutcome;
    }

    private OperationOutcome doAsync(HttpServletRequest request, HttpServletResponse response, String resourceType, String id, String params, Map<String, String> queryParams){
        logger.info("Async call");

        String sessionId = asyncService.newAyncGetSession( request.getRequestURL().toString(), fhirServer, resourceType, id, params, queryParams );
        String callUrl = request.getRequestURL().toString();

        logger.info("Ansync request detected");
        String sessionUrl = callUrl.substring(0, callUrl.indexOf("fhir/"))+"async-services/"+sessionId;

        response.setStatus(202);
        response.setHeader( CONTENT_LOCATION, sessionUrl );
        OperationOutcome operationOutcome = new OperationOutcome();
        operationOutcome.addIssue()
            .setSeverity( OperationOutcome.IssueSeverity.INFORMATION)
            .setCode(OperationOutcome.IssueType.VALUE)
            .addLocation(sessionUrl)
        ;

        return operationOutcome;
    }

    public void setFhirServerUrl(String fhirServerUrl) {
        logger.info("FHIR server set to "+fhirServerUrl);
        this.fhirServer = new FhirServer( fhirServerUrl );
        this.fhirServer.getFhirOperationRepository().registerOperation( new PatientExportOperation(fhirServerUrl));
        this.fhirServer.getFhirOperationRepository().registerOperation( new GroupExportOperation(fhirServerUrl));
    }
}
