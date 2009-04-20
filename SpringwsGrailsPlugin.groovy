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

import org.codehaus.groovy.grails.plugins.spring.ws.EndpointArtefactHandler
import org.codehaus.groovy.grails.plugins.spring.ws.GrailsEndpointClass

import org.springframework.ws.server.endpoint.mapping.PayloadRootQNameEndpointMapping

import org.springframework.beans.factory.config.MethodInvokingFactoryBean

/**
 * Plugin that introduces some conventions for creating Spring WS based, best practice
 * web services.
 *
 * @author Russ Miles (russ@russmiles.com)
 * 
 */
class SpringwsGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [functionalTest:'1.2.5 > *']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            'grails-app/views/error.gsp',
			'grails-app/endpoints/HolidayEndpoint.groovy',
			'test/functional/HolidayEndpointFunctionalTests.groovy'
    ]

    def author = "Russ Miles"
    def authorEmail = "russ@russmiles.com"
   	def title = "This plugin adds contract driven web service capabilities to a Grails application."
    def description = '''\\
    	Spring Web Services plugin allows your Grails application to provide and consume 
    	contract-driven web services. On the service provision side, a first-class endpoint
    	artefact is introduced including functional tests for endpoints. When consuming services,
    	a WebServiceTemplate is provided, in much the same vein as the Spring WebServiceTemplate,
    	that integrates more naturally with the rest of your Groovy code'''

    def documentation = "http://grails.org/SpringWs+Plugin"

    def artefacts = [new EndpointArtefactHandler()]

    def doWithSpring = {
		// Add a mappings collection
        def defaultMappings = [:]
        application.endpointClasses.each { endpointClass ->
	        def fullName = endpointClass.fullName
	        def name = endpointClass.name
	        def propertyName = endpointClass.propertyName
	
	        "${fullName}"(MethodInvokingFactoryBean) {
	            targetObject = ref("grailsApplication", true)
	            targetMethod = "getArtefact"
	            arguments = [EndpointArtefactHandler.TYPE, fullName]
	        }
			defaultMappings["{${endpointClass.getClazz().namespace}}${name}Request"] = ref("${fullName}")
    	}

		// Payload mapper
        "payloadRootQNameEndpointMapping"(PayloadRootQNameEndpointMapping) {
        	mappings = defaultMappings
		}
    }
    
    def doWithWebDescriptor = { xml ->
		// servlets
	    def servlets = xml.servlet
	    servlets[servlets.size() - 1] + {
	        servlet {
	            'servlet-name'("web-services")
	            'display-name'("web-services")
	            'servlet-class'("org.springframework.ws.transport.http.MessageDispatcherServlet")
	        }
	    }
	    // servlet mappings
	    def servletMappings = xml.'servlet-mapping'
	    servletMappings[servletMappings.size() - 1] + {
	        'servlet-mapping' {
	            'servlet-name'("web-services")
	            'url-pattern'("/services/*")
	        }
	    }
    }
}
