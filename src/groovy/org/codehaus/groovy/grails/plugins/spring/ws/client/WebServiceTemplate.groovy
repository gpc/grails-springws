package org.codehaus.groovy.grails.plugins.spring.ws.client

import org.springframework.xml.transform.StringResult
import org.springframework.xml.transform.StringSource

class WebServiceTemplate extends org.springframework.ws.client.core.WebServiceTemplate{
	org.springframework.ws.client.core.WebServiceTemplate webServiceTemplate
     
	WebServiceTemplate() {
	  this.webServiceTemplate = new org.springframework.ws.client.core.WebServiceTemplate()
	}

    String sendToEndpoint(String uri, String request) {
        StringResult result = new StringResult();
        StringSource source = new StringSource(request as String);
        super.sendSourceAndReceiveToResult(uri, source, result);
        result.toString()
    }

    void setDefaultUri(String serviceUri) {
        super.setDefaultUri(serviceUri);
    }
}
