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
import org.codehaus.groovy.grails.plugins.spring.ws.InterceptorsConfigArtefactHandler
import org.codehaus.groovy.grails.plugins.spring.ws.GrailsEndpointClass
import org.codehaus.groovy.grails.plugins.spring.ws.EndpointInterceptorAdapter
import org.codehaus.groovy.grails.plugins.spring.ws.DefaultEndpointAdapter
import org.codehaus.groovy.grails.plugins.spring.ws.ReloadablePayloadRootQNameEndpointMapping

import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection
import org.apache.commons.logging.LogFactory

/**
 * Plugin that introduces some conventions for creating Spring WS based, best practice
 * web services.
 *
 * @author Russ Miles (russ@russmiles.com)
 * @author Ivo Houbrechts (ivo@houbrechts-it.be)
 */
class SpringwsGrailsPlugin {
    // the plugin version
    def version = "0.2.2"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [functionalTest:'1.2.5 > *']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
			'grails-app/endpoints/*',
			'test/functional/*',
			'test/unit/*',
			'test/integration/*',
			'soapui/*'
    ]

    def author = "Russ Miles"
    def authorEmail = "russ@russmiles.com"
   	def title = "This plugin adds contract driven web service capabilities to a Grails application."
    def description = '''\\
    	Spring Web Services plugin allows your Grails application to provide and consume 
    	contract-driven web services. Feature highlights include: 
		* New in 0.2.2: Added configuration option to override default Endpoint-name-based strategy for mapping incoming XML payloads to endpoints
		* New in 0.2.1: Fixed concurrency bug in DefaultEndpointAdapter (see http://jira.codehaus.org/browse/GRAILSPLUGINS-1208)
		* New in 0.2.1: Upgraded to Spring Web Services 1.5.7 (see http://jira.codehaus.org/browse/GRAILSPLUGINS-1208)
		* Endpoint Interceptors so that you can now introduce
		  common logic in an unintrusive fashion across a specified range of endpoints
		* You can export the wsdl for a given endpoint by, in the simplest case,  
		  setting the springws.wsdl.<your endpoint name, without the Endpoint bit>.export
		* The EndpointFunctionalTestCase offers a more groovy withEndpointRequest closure 
		  that significantly tightens up your endpoint functional test code.
        * On the service provision side, a first-class endpoint	artefact is introduced including 
          functional tests for endpoints.
        * When consuming services, a WebServiceTemplate is provided, in much the same vein as 
          the Spring WebServiceTemplate, that integrates more naturally with the rest of your Groovy code.'''

    def documentation = "http://grails.org/SpringWs+Plugin"

    def artefacts = [EndpointArtefactHandler, InterceptorsConfigArtefactHandler]

	def watchedResources = "file:./grails-app/endpoints/**/*"
	
	def log = LogFactory.getLog(SpringwsGrailsPlugin)
	
	static final ENDPOINT_BEANS = { endpoint ->
	    "${endpoint.fullName}"(endpoint.clazz) { bean ->
            bean.singleton = true
            bean.autowire = "byName"
        }
    }

	static final INTERCEPTOR_BEANS = { interceptor ->
	    "${interceptor.fullName}Class"(MethodInvokingFactoryBean) {
            targetObject = ref("grailsApplication", true)
            targetMethod = "getArtefact"
            arguments = [InterceptorsConfigArtefactHandler.TYPE, interceptor.fullName]
        }
        "${interceptor.fullName}"(interceptor.clazz) { bean ->
            bean.singleton = true
            bean.autowire = "byName"
        }
    }

    def doWithSpring = {
		// Add each of the endpoints
		for(endpointClass in application.getArtefacts(EndpointArtefactHandler.TYPE)) {
		    def name= endpointClass.name
            def callable = ENDPOINT_BEANS.curry(endpointClass)
            callable.delegate = delegate
            callable.call()

            def wsdlConfig= application.config.springws?.wsdl?."$name"
            if(wsdlConfig){
                log.debug("exporting wsdl for $name")
                "${wsdlConfig.wsdlName ?: name}"(DefaultWsdl11Definition){
                    schemaCollection = {CommonsXsdSchemaCollection s->
                        xsds= (wsdlConfig.xsds)? wsdlConfig.xsds.split(',') : "/WEB-INF/${name}.xsd"
                    }
                    portTypeName = wsdlConfig.portTypeName ?: "${name}Port"
                    serviceName = wsdlConfig.serviceName ?: "${name}Service"
                    locationUri = wsdlConfig.locationUri ?: "${application.config.grails.serverURL ?: 'http://localhost:8080/' + application.metadata['app.name']}/services/${name}Request"
                    targetNamespace = wsdlConfig.targetNamespace ?: "${endpointClass.getClazz().namespace}/definitions"
                }
            }
        }
		
		// Add each of the interceptors
		for(interceptorsClass in application.getArtefacts(InterceptorsConfigArtefactHandler.TYPE)) {
            def callable = INTERCEPTOR_BEANS.curry(interceptorsClass)
            callable.delegate = delegate
            callable.call()
     	}

		// Payload mapper
        "payloadRootQNameEndpointMapping"(ReloadablePayloadRootQNameEndpointMapping)
    }

	def doWithApplicationContext = { applicationContext ->
        reload(application, applicationContext)
    }

	def onChange = { event ->
	    if (log.debugEnabled) log.debug("onChange: ${event}")
	
        if(event.source.toString().endsWith('Endpoint')) {
            def newEndpoint = event.application.addArtefact(EndpointArtefactHandler.TYPE, event.source)
            beans(ENDPOINT_BEANS.curry(newEndpoint)).registerBeans(event.ctx)
        } else if(event.source.toString().endsWith('Interceptors')) {
            def newInterceptor = event.application.addArtefact(InterceptorsConfigArtefactHandler.TYPE, event.source)
            beans(INTERCEPTOR_BEANS.curry(newInterceptor)).registerBeans(event.ctx)
        }

        reload(event.application, event.ctx)
	}
	
    private reload(GrailsApplication application, applicationContext) {
        log.info("reloadEndpoints")
        def defaultMappings = [:]
        for(endpointClass in application.getArtefacts(EndpointArtefactHandler.TYPE)) {
            def endpoint = applicationContext.getBean("${endpointClass.fullName}")
            def adapter= new DefaultEndpointAdapter(endpointImpl: endpoint, name: endpointClass.logicalPropertyName)
            def requestElement
            if(GrailsClassUtils.isStaticProperty(endpointClass.getClazz(), 'requestElement')){
                requestElement= endpointClass.getClazz().requestElement
            }else{
                requestElement= "${endpointClass.name}Request"
            }
            defaultMappings["{${endpointClass.getClazz().namespace}}${requestElement}"] = adapter
        }

        if (log.debugEnabled) log.debug("resulting mappings: ${defaultMappings}")
        applicationContext.getBean('payloadRootQNameEndpointMapping').registerEndpoints(defaultMappings)

        log.info("reloadInterceptors")
        def interceptors = []
        for(ic in application.getArtefacts(InterceptorsConfigArtefactHandler.TYPE)) {
            def interceptorClass = applicationContext.getBean("${ic.fullName}Class")
            def bean = applicationContext.getBean(ic.fullName)
            for(interceptorConfig in interceptorClass.getConfigs(bean)) {
                interceptors << new EndpointInterceptorAdapter(interceptorConfig:interceptorConfig, configClass:bean)
            }
        }
        if (log.debugEnabled) log.debug("resulting interceptors: ${interceptors}")
        applicationContext.getBean('payloadRootQNameEndpointMapping').interceptors = interceptors
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
