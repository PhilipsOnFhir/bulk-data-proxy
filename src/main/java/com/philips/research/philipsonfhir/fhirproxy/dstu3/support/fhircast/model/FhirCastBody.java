package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FhirCastBody {
    // subscribe
    String hub_callback;
    String hub_mode;
    String hub_topic;
    String hub_secret;
    String hub_events;
    String hub_lease_seconds;

    // event
    String timestamp;
    String id;
    FhirCastWorkflowEventEvent event;

    public boolean isSubscribe(){
        return hub_callback!=null;
    }

    public FhirCastSessionSubscribe getFhirCastSessionSubscribe(){
        FhirCastSessionSubscribe fhirCastSessionSubscribe = new FhirCastSessionSubscribe();
        fhirCastSessionSubscribe.hub_callback = hub_callback;
        fhirCastSessionSubscribe.hub_mode     = hub_mode;
        fhirCastSessionSubscribe.hub_topic    = hub_topic;
        fhirCastSessionSubscribe.hub_secret   = hub_secret;
        fhirCastSessionSubscribe.hub_events   = hub_events;
        fhirCastSessionSubscribe.hub_lease_seconds = hub_lease_seconds;
        return fhirCastSessionSubscribe;
    }

    public boolean isEvent(){
        return event!=null;
    }

    public FhirCastWorkflowEvent getFhirCastWorkflowEvent(){
        FhirCastWorkflowEvent fhirCastWorkflowEvent = new FhirCastWorkflowEvent();
        fhirCastWorkflowEvent.timestamp = timestamp;
        fhirCastWorkflowEvent.id = id;
        fhirCastWorkflowEvent.event = event;
        return  fhirCastWorkflowEvent;
    }
}
