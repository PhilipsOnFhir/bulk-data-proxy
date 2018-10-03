package com.philips.research.philipsonfhir.fhirproxy.applications.basic.controller;

import com.philips.research.philipsonfhir.fhirproxy.support.proxy.controller.SampleFhirGateway;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyFhirController extends SampleFhirGateway {
    MyFhirController(){
        super( "http://localhost:9500/baseDstu3" );
//        super( "http://measure.eval.kanvix.com/cqf-ruler/baseDstu3" );
//        super( "http://localhost:9001/cqf-ruler/baseDstu3" );
    }
//    MyFhirController(){
//    }
}
