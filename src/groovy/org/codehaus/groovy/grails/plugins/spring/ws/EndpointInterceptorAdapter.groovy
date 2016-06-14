/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.grails.plugins.spring.ws

import java.util.regex.Pattern
import org.apache.commons.logging.LogFactory
import org.springframework.ws.context.MessageContext
import org.springframework.ws.server.EndpointInterceptor
import org.springframework.ws.soap.server.SoapEndpointInterceptor
import org.springframework.ws.soap.SoapHeaderElement

/**
 * Implementation of  {@link EndpointInterceptor}  that delegates to an Interceptors artefact
 *
 * @author Ivo Houbrechts (ivo@houbrechts-it.be)
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 *
 */
public class EndpointInterceptorAdapter implements SoapEndpointInterceptor {
	
    def interceptorConfig
    def configClass

    def endpoineRegex
    def interceptorList

    private static def log = LogFactory.getLog(EndpointInterceptorAdapter)

	public boolean understands(SoapHeaderElement element) {
	   // ask each soap interceptor if it understands the header element
       for (interceptor in interceptorConfig.interceptorList) {
           if (interceptor instanceof SoapEndpointInterceptor && interceptor.understands(element)) {
               return true
           }
       }
       return false
    }

    public boolean handleRequest(MessageContext messageContext, Object endPointClass) {
        try {
            if (accept(endPointClass.name)) {
                if (interceptorConfig.handleRequest) {
                    if (!interceptorConfig.handleRequest(messageContext, endPointClass))
                        return false
                }
                for (interceptor in interceptorConfig.interceptorList) {
                    if (!interceptor.handleRequest(messageContext, endPointClass))
                        return false
                }
            }
            return true
        } catch (e) {
            log.error("exception in handleRequest for endpoint: ${endPointClass.name}, interceptors: $configClass", e)
            return false
        }
    }

    public boolean handleResponse(MessageContext messageContext, Object endPointClass) {
        try {
            if (accept(endPointClass.name)) {
                for (interceptor in interceptorConfig.interceptorList?.reverse()) {
                    if (!interceptor.handleResponse(messageContext, endPointClass))
                        return false
                }
                if (interceptorConfig.handleResponse) {
                    if (!interceptorConfig.handleResponse(messageContext, endPointClass))
                        return false
                }
            }
            return true
        } catch (e) {
            log.error("exception in handleResponse for endpoint: ${endPointClass.name}, interceptors: $configClass", e)
            return false
        }
    }

    public boolean handleFault(MessageContext messageContext, Object endPointClass) {
        try {
            if (accept(endPointClass.name)) {
                for (interceptor in interceptorConfig.interceptorList?.reverse()) {
                    if (!interceptor.handleFault(messageContext, endPointClass))
                        return false
                }
                if (interceptorConfig.handleFault) {
                    if (!interceptorConfig.handleFault(messageContext, endPointClass))
                        return false
                }
            }
            return true
        } catch (e) {
            log.error("exception in handleFault for endpoint: ${endPointClass.name}, interceptors: $configClass", e)
            return false
        }
    }


    boolean accept(String endpointName) {
        if (endpoineRegex == null) {
            def scope = interceptorConfig.scope

            if (scope) {
                endpoineRegex = Pattern.compile(scope.replaceAll("\\*", ".*"))
            }
            else {
                endpoineRegex = Pattern.compile(".*")
            }
        }

        return endpoineRegex.matcher(endpointName).matches()
    }

    void afterCompletion(MessageContext messageContext, Object endPointClass, Exception e){
        //TODO: Implement afterCompletion here
    }

    String toString() {
        return "EndpointInterceptorAdapter[$interceptorConfig, $configClass]"
    }
}