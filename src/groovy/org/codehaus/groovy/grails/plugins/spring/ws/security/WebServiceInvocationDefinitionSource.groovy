package org.codehaus.groovy.grails.plugins.spring.ws.security

import org.springframework.security.ConfigAttributeDefinition
import org.springframework.security.intercept.ObjectDefinitionSource
import org.springframework.security.util.UrlMatcher
import org.springframework.security.util.UrlUtils
import org.apache.commons.logging.*
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest


/**
 *
 * @author tareq.abedrabbo
 */
class WebServiceInvocationDefinitionSource implements ObjectDefinitionSource{

    def log = LogFactory.getLog(WebServiceInvocationDefinitionSource)

    private Map requestMap = new LinkedHashMap()

    UrlMatcher urlMatcher


    public WebServiceInvocationDefinitionSource(){

    }
    
    public WebServiceInvocationDefinitionSource(UrlMatcher urlMatcher, LinkedHashMap requestMap) {
        this.urlMatcher = urlMatcher
        requestMap.each{
            addSecureUrl(it.key, it.value)
        }
    }

    public void addSecureUrl(String pattern, ConfigAttributeDefinition attr) {
        requestMap[urlMatcher.compile(pattern)] =  attr
    }

    public Collection getConfigAttributeDefinitions() {
        return Collections.unmodifiableCollection(requestMap.values())
    }

    public ConfigAttributeDefinition getAttributes(Object object) throws IllegalArgumentException {
        if (!object || !supports(object.class)) {
            throw new IllegalArgumentException('Object must be a GrailsWebRequest');
        }

        def request = object.currentRequest
        def url = "${request.servletPath}${request.pathInfo?:''}"
        
        return lookupAttributes(url)
    }

    private ConfigAttributeDefinition lookupAttributes(String url) {

        if (urlMatcher.requiresLowerCaseUrl()) {
            url = url.toLowerCase()
        }

        for(entry in requestMap.entrySet()){
            def pattern = entry.key
            def matched = urlMatcher.pathMatchesUrl(pattern, url)
            log.debug "Candidate is: '$url'; pattern is $pattern; matched= $matched; value= ${entry.value}"
            if(matched){
                return entry.value
            }
        }
        return null
    }

    public boolean supports(Class clazz) {
        return GrailsWebRequest.isAssignableFrom(clazz)
    }

    public boolean isConvertUrlToLowercaseBeforeComparison() {
        return urlMatcher.requiresLowerCaseUrl()
    }
}

