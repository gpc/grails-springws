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

import org.codehaus.groovy.grails.plugins.spring.ws.client.WebServiceTemplate

import groovy.xml.MarkupBuilder

import org.codehaus.groovy.grails.plugins.spring.ws.security.WsSecurityConfigFactory

/**
 * Convenience endpoint functional test base class.
 *
 * @author Russ Miles (russ@russmiles.com)
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 *
 */
public class EndpointFunctionalTestCase extends GroovyTestCase {

    def webServiceTemplate
    def consoleOutput

    void setUp(){
        webServiceTemplate = new WebServiceTemplate()
    }
	
	def withEndpointRequest = { url, payload ->
		def writer = new StringWriter()
	    def request = new MarkupBuilder(writer)
		payload.delegate = request
		payload.call()
		
		def response = webServiceTemplate.sendToEndpoint(url, writer.toString())
	    new XmlSlurper().parseText(response)
	}
	
	// accepts a security config instance and applies the resulting security interceptor
    def withSecuredEndpointRequest = { url, wsSecurityConfig, payload ->
		def writer = new StringWriter()
	    def request = new MarkupBuilder(writer)
		payload.delegate = request
		payload.call()

        // set the security Interceptor
        def securityInterceptor = WsSecurityConfigFactory.createInterceptor(securityConfigClass: wsSecurityConfig)
        def swsTemplate =  webServiceTemplate.webServiceTemplate
        swsTemplate.interceptors = [securityInterceptor]

		def response = webServiceTemplate.sendToEndpoint(url, writer.toString())

        // clean up
        swsTemplate.interceptors = [securityInterceptor]

	    new XmlSlurper().parseText(response)
	}
}