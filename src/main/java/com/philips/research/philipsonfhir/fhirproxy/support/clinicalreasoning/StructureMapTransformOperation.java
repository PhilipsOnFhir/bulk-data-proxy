package com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.processor.StructureMapTransformServer;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;

import java.util.Map;
import java.util.Optional;

public class StructureMapTransformOperation extends FhirResourceInstanceOperation {
    private final IGenericClient client;
    private final String url;

    public StructureMapTransformOperation(String url, IGenericClient client) throws FHIRException {
        super( ResourceType.StructureMap.name(), "$transform" );
        this.client = client;
        this.url = url;
    }

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parameters, Map<String, String> queryParams) {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );
                IdType idType = new IdType().setValue( resourceType + "/" + resourceId );

                String source = queryParams.get( "source" );

                Optional<Resource> optResource = ((Parameters) parameters).getParameter().stream()
                    .filter( parameter -> parameter.getName().equals( "content" ) )
                    .map( parameter -> parameter.getResource() )
                    .findFirst();

                if ( !optResource.isPresent() ) {
                    throw new FHIRException( "missind content parameter" );
                }
                Resource contentRsource = optResource.get();

                StructureMapTransformServer structureMapTransformServer = new StructureMapTransformServer( client );

                IBaseResource result = structureMapTransformServer.doTransform( resourceId, contentRsource, null );
                return result;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public Map<String, OperationOutcome> getErrors() {
                return null;
            }
        };
    }
}
