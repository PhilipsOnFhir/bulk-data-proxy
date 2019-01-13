package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.controller;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model.FhirCastSessionSubscribe;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model.FhirCastWorkflowEvent;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class MyFhirCastService {
    private Map<String, FhirCastSession> sessions = new TreeMap<>();
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public FhirCastSession createFhirCastSession() {
        String sessionId = "FC"+System.currentTimeMillis();
        FhirCastSession fhirCastSession = new FhirCastSession( sessionId );

        sessions.put( sessionId, fhirCastSession );
        logger.info("create session"+sessionId);
        return fhirCastSession;
    }

    public Collection<FhirCastSession> getActiveFhirCastSessions() {
        return sessions.values();
    }

    public void deleteFhirCastSession(String sessionId)  {
        logger.info("remove session"+sessionId);
        try {
            FhirCastSession fhirCastSession = getFhirCastSession(sessionId);
            sessions.remove( sessionId );
            //TODO send remove events.
        } catch ( FhirCastException e ) {
        }
    }

    private FhirCastSession getFhirCastSession(String sessionId) throws FhirCastException {
        FhirCastSession fhirCastSession = sessions.get( sessionId );
        if ( fhirCastSession !=null ){
            return fhirCastSession;
        }
        throw new FhirCastException( "UnknownSessionId" );
    }

    public void subscribe(String sessionId, FhirCastSessionSubscribe fhirCastSessionSubscribe) throws FhirCastException {
        logger.info("update session"+sessionId);
        FhirCastSession fhirCastSession = getFhirCastSession( sessionId );
        fhirCastSession.subscribe( fhirCastSessionSubscribe );
    }

    public void sendEvent(String sessionId, FhirCastWorkflowEvent fhirCastWorkflowEvent) throws FhirCastException {
        FhirCastSession fhirCastSession = getFhirCastSession( sessionId );
        fhirCastSession.sendEvent( fhirCastWorkflowEvent );
    }

    public Map<String, String> getContext(String sessionId) throws FhirCastException {
        FhirCastSession fhirCastSession = getFhirCastSession( sessionId );
        return fhirCastSession.getContext();
    }

    public void updateFhirCastSession(String sessionId) {
        FhirCastSession fhirCastSession = null;
        try {
            fhirCastSession = getFhirCastSession( sessionId );
        } catch ( FhirCastException e ) {
            fhirCastSession = new FhirCastSession( sessionId );
            sessions.put( sessionId, fhirCastSession );
        }
    }
}
