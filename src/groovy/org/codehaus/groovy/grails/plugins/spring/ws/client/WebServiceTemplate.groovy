package org.codehaus.groovy.grails.plugins.spring.ws.client

import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.xml.transform.StringResult
import org.springframework.xml.transform.StringSource

@Category(WebServiceTemplate) class WebServiceTemplate {

    String sendToEndpoint(String request) {
        StringResult result = new StringResult();
        StringSource source = new StringSource(request as String);
        this.sendSourceAndReceiveToResult(source, result);
        result.toString()
    }

    void setDefaultUri(String serviceUri) {
        this.setDefaultUri(serviceUri);
    }
}
