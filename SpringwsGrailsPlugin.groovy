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



import grails.util.GrailsNameUtils
import grails.util.Holders
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.plugins.spring.ws.*
import org.codehaus.groovy.grails.plugins.spring.ws.security.WebServiceInvocationDefinitionSource
import org.codehaus.groovy.grails.plugins.spring.ws.security.WsSecurityConfigArtefactHandler
import org.codehaus.groovy.grails.plugins.spring.ws.security.WsSecurityConfigFactory
import org.springframework.beans.factory.config.MethodInvokingFactoryBean
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection

/**
 * Plugin that introduces some conventions for creating Spring WS based, best practice
 * web services.
 *
 * @author Russ Miles (russ@russmiles.com)
 * @author Ivo Houbrechts (ivo@houbrechts-it.be)
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class SpringwsGrailsPlugin {
    def version = "1.0.0"
    def grailsVersion = "2.0 > *"
    //def dependsOn = [functionalTest:'1.2.5 > *']
    def pluginExcludes = [
            'grails-app/endpoints/*',
            'grails-app/conf/WsSecurityConfig.groovy',
            'test/functional/*',
            'test/unit/*',
            'test/integration/*',
            'soapui/*'
    ]
    def author = "Russ Miles"
    def authorEmail = "russ@russmiles.com"
    def title = "Spring WS Plugin"
    def description = 'Spring WS Plugin'
    def documentation = "http://grails.org/SpringWs+Plugin"
    def license = "APACHE"
    def issueManagement = [system: "JIRA", url: "http://jira.grails.org/browse/GPSPRINGWS"]
    def scm = [url: "https://github.com/gpc/grails-springws"]
    def developers = [[name: "Dhiraj Mahapatro", email: "dmahapatro@netjets.com"]]
    def artefacts = [EndpointArtefactHandler, InterceptorsConfigArtefactHandler, WsSecurityConfigArtefactHandler]
    def watchedResources = ["file:./grails-app/endpoints/**/*",
            "file:./grails-app/conf/*WsSecurityConfig.groovy"]
    def loadAfter = ['acegi']

    def log = LogFactory.getLog(SpringwsGrailsPlugin)

    static final DEFAULT_WS_SECURITY_CONFIG_NAME = 'WsSecurityConfig'

    static final ENDPOINT_BEANS = { endpoint ->
        "${endpoint.fullName}"(endpoint.clazz) { bean ->
            bean.singleton = true
            bean.autowire = "byName"
        }
    }

    static final SECURITY_CONFIG_BEANS = { config ->
        "$config.fullName"(config.clazz) { bean ->
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
        for (endpointClass in application.getArtefacts(EndpointArtefactHandler.TYPE)) {
            def name = endpointClass.name
            def callable = ENDPOINT_BEANS.curry(endpointClass)
            callable.delegate = delegate
            callable.call()

            def wsdlConfig = application.config.springws?.wsdl?."$name"
            if (wsdlConfig) {
                log.debug("exporting wsdl for $name")
                "${wsdlConfig.wsdlName ?: name}"(DefaultWsdl11Definition) {
                    schemaCollection = { CommonsXsdSchemaCollection s ->
                        xsds = (wsdlConfig.xsds) ? wsdlConfig.xsds.split(',') : "/WEB-INF/${name}.xsd"
                        inline = wsdlConfig.inline ?: false
                    }
                    portTypeName = wsdlConfig.portTypeName ?: "${name}Port"
                    serviceName = wsdlConfig.serviceName ?: "${name}Service"
                    locationUri = wsdlConfig.locationUri ?: "${application.config.grails.serverURL ?: 'http://localhost:8080/' + application.metadata['app.name']}/services/${name}Request"
                    targetNamespace = wsdlConfig.targetNamespace ?: "${endpointClass.getClazz().namespace}/definitions"
                }
            }
        }

        // Add ws-security for each of the applicable classes
        for (wsSecurityConfigClass in application.wsSecurityConfigClasses) {
            log.debug "found WS-Security configuration class: ${wsSecurityConfigClass.fullName}"
            def callable = SECURITY_CONFIG_BEANS.curry(wsSecurityConfigClass)
            callable.delegate = delegate
            callable.call()
        }

        // Configure the applicable key stores
        def keyStores = application.config.springws?.security?.keyStore
        log.debug "key stores: ${keyStores.entrySet().inspect()}"
        for (keyStore in keyStores) {
            def bean = WsSecurityConfigFactory.createKeyStoreBean(keyStore)
            bean.delegate = delegate
            bean.call()
        }

        // if Spring Security is installed, add access decision beans
        def foundAcegi = Holders.getPluginManager()?.hasGrailsPlugin('acegi')
        if (foundAcegi) {
            wsSecurityRoleVoter(SpringwsGrailsPlugin.classLoader.loadClass('org.springframework.security.vote.RoleVoter')) {}
            wsSecurityAccessDecisionManager(SpringwsGrailsPlugin.classLoader.loadClass('org.springframework.security.vote.AffirmativeBased')) {
                decisionVoters = [wsSecurityRoleVoter]
            }
            wsSecurityObjectDefinitionSource(WebServiceInvocationDefinitionSource) {}
        }

        // Add each of the interceptors
        for (interceptorsClass in application.getArtefacts(InterceptorsConfigArtefactHandler.TYPE)) {
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

        if (event.source.toString().endsWith('Endpoint')) {
            def newEndpoint = event.application.addArtefact(EndpointArtefactHandler.TYPE, event.source)
            beans(ENDPOINT_BEANS.curry(newEndpoint)).registerBeans(event.ctx)
        } else if (event.source.toString().endsWith('Interceptors')) {
            def newInterceptor = event.application.addArtefact(InterceptorsConfigArtefactHandler.TYPE, event.source)
            beans(INTERCEPTOR_BEANS.curry(newInterceptor)).registerBeans(event.ctx)
        } else if (event.source.toString().endsWith('WsSecurityConfig')) {
            def newWsSecurityConfig = event.application.addArtefact(WsSecurityConfigArtefactHandler.TYPE, event.source)
            beans(SECURITY_CONFIG_BEANS.curry(newWsSecurityConfig)).registerBeans(event.ctx)
        }

        reload(event.application, event.ctx)
    }

    private reload(GrailsApplication application, applicationContext) {
        log.info("reloadEndpoints")
        def defaultMappings = [:]
        for (endpointClass in application.getArtefacts(EndpointArtefactHandler.TYPE)) {
            def endpoint = applicationContext.getBean("${endpointClass.fullName}")
            def adapter = new DefaultEndpointAdapter(endpointImpl: endpoint, name: endpointClass.logicalPropertyName)
            def requestElement
            if (GrailsClassUtils.isStaticProperty(endpointClass.getClazz(), 'requestElement')) {
                requestElement = endpointClass.getClazz().requestElement
            } else {
                requestElement = "${endpointClass.name}Request"
            }
            defaultMappings["{${endpointClass.getClazz().namespace}}${requestElement}"] = adapter
        }

        if (log.debugEnabled) log.debug("resulting mappings: ${defaultMappings}")
        applicationContext.getBean('payloadRootQNameEndpointMapping').registerEndpoints(defaultMappings)

        log.info("reloadInterceptors")
        def interceptors = []
        for (ic in application.getArtefacts(InterceptorsConfigArtefactHandler.TYPE)) {
            def interceptorClass = applicationContext.getBean("${ic.fullName}Class")
            def bean = applicationContext.getBean(ic.fullName)
            for (interceptorConfig in interceptorClass.getConfigs(bean)) {
                interceptors << new EndpointInterceptorAdapter(interceptorConfig: interceptorConfig, configClass: bean)
            }
        }

        def foundAcegi = Holders.getPluginManager()?.hasGrailsPlugin('acegi')
        def acegiActive
        def acegiConfig
        def foundSecurityBeans = foundAcegi && applicationContext.authenticationManager && applicationContext.userDetailsService && applicationContext.userCache
        def authenticationManager, userDetailsService, userCache, accessDecisionManager, objectDefinitionSource
        if (foundAcegi) {
            log.debug 'Found Spring Security (acegi plugin)'

            //loading acegi plugin config
            acegiConfig = SpringwsGrailsPlugin.classLoader.loadClass('org.codehaus.groovy.grails.plugins.springsecurity.AuthorizeTools').securityConfig.security
            // checking whether acegi plugin is active
            acegiActive = acegiConfig?.active

            if (acegiActive) {
                log.debug 'Spring Security plugin is active.'

                if (foundSecurityBeans) {
                    authenticationManager = applicationContext.authenticationManager
                    userDetailsService = applicationContext.userDetailsService
                    accessDecisionManager = applicationContext.wsSecurityAccessDecisionManager
                    userCache = applicationContext.userCache
                    objectDefinitionSource = applicationContext.wsSecurityObjectDefinitionSource
                    log.debug "Using authenticationManager: $authenticationManager"
                    log.debug "Using userDetailsService: $userDetailsService"
                    log.debug "Using userCache: $userCache"
                    log.debug "Using accessDecisionManager: $accessDecisionManager"
                    log.debug "Using objectDefinitionSource: $objectDefinitionSource"

                    //TODO add checks on the type of configured authorization
                    objectDefinitionSource.urlMatcher = applicationContext.filterInvocationInterceptor.objectDefinitionSource.urlMatcher

                    // copy urls that start with /services/ to our objectDefinitionSource
                    if (!acegiConfig.useRequestMapDomainClass && acegiConfig.requestMapString) {
                        log.debug 'Using requestMapString for authorization.'
                        applicationContext.filterInvocationInterceptor.objectDefinitionSource.requestMap.each {
                            if (it.key.startsWith('/services/')) {
                                objectDefinitionSource.addSecureUrl(it.key, it.value)
                            }
                        }
                    }

                    // replace password encoder
                    log.warn 'Replacing digest by plain text password encoder. All passwords will be stored in clear text in the database.'
                    def plaintextPasswordEncoderClass = SpringwsGrailsPlugin.classLoader.loadClass('org.springframework.security.providers.encoding.PlaintextPasswordEncoder')
                    def encoder = plaintextPasswordEncoderClass.newInstance()
                    applicationContext.daoAuthenticationProvider?.passwordEncoder = encoder
                    applicationContext.authenticateService?.passwordEncoder = encoder

                    // exclude /services/** from the FilterChainProxy by providing a more specific url
                    def filterChain = applicationContext.springSecurityFilterChain
                    def serviceFilters = []
                    def newChainMap = ['/services/**': serviceFilters] as LinkedHashMap

                    newChainMap.putAll(filterChain.filterChainMap)
                    filterChain.filterChainMap = newChainMap
                    log.debug "Excluded /services/** from the security filter chain. Resulting mapping: ${filterChain.filterChainMap}"
                } else {
                    //TODO better message: suggest running the create-auth-domains script
                    log.warn 'Security beans not found. Make sure the acegi plugin is active and the authentication domain is generated.'
                }
            } else {
                log.debug "Spring Security plugin is not active."
            }
        } else {
            log.debug 'Spring Security plugin not found.'
        }

        log.debug("Reloading security config")
        def foundDefaultWsSecurityConfig = applicationContext."$DEFAULT_WS_SECURITY_CONFIG_NAME" && application.isArtefactOfType(DEFAULT_WS_SECURITY_CONFIG_NAME, 'WsSecurityConfig')
        log.debug "Found default WS-Security config: $foundDefaultWsSecurityConfig"
        for (wsSecurityConfigClass in application.wsSecurityConfigClasses) {
            log.debug "Processing ${wsSecurityConfigClass.dump()}"
            def config = applicationContext."${wsSecurityConfigClass.fullName}"
            log.debug "Creating WS-Security interceptor for ${config.class.name}. Default: ${config.class.name == DEFAULT_WS_SECURITY_CONFIG_NAME}"
            def params = [securityConfigClass: config]
            // integration with the Spring Security Plugin
            if (foundSecurityBeans) {
                params['authenticationManager'] = authenticationManager
                params['userDetailsService'] = userDetailsService
                params['userCache'] = userCache
                params['accessDecisionManager'] = accessDecisionManager
                params['objectDefinitionSource'] = objectDefinitionSource
            }

            def securityInterceptor = WsSecurityConfigFactory.createInterceptor(params)
            log.debug "Created WS-Security interceptor: ${securityInterceptor.dump()}"
            // detect referent endpoints
            def referents = []
            for (endpointClass in application.endpointClasses) {
                if (GrailsClassUtils.isStaticProperty(endpointClass.clazz, 'wsSecurity')) {
                    def endpointName = GrailsNameUtils.getPropertyName(endpointClass.name)
                    def wsSecurity = endpointClass.clazz.wsSecurity
                    // check for default config class i.e. static wsSecurity = true
                    if ((wsSecurity instanceof Boolean)) {
                        if (wsSecurity) {
                            // check that a default config class exists
                            if (!foundDefaultWsSecurityConfig) {
                            }
                            // if this is the default config, use it
                            if (config.class.name == DEFAULT_WS_SECURITY_CONFIG_NAME) {
                                referents << endpointName
                            }
                        }
                    } else {
                        if (wsSecurity == config.class) {
                            referents << endpointName
                        } else {
                            throw new IllegalArgumentException("${endpointName}.wsSecurity must either be of boolean type or point to WS-Security config class")
                        }
                    }
                }
            }
            log.debug "Endpoints using ${config.class.name}: ${referents.inspect()}"
            def regexp = referents.join('|')
            if (referents) {
                def interceptorConfig = new InterceptorConfig(name: wsSecurityConfigClass.fullName, interceptorList: [securityInterceptor], scope: regexp, initialised: true)
                def interceptorAdapter = new EndpointInterceptorAdapter(interceptorConfig: interceptorConfig)
                // inject it in the interceptors chain
                def order = GrailsClassUtils.isStaticProperty(config.class, 'order') ? config.order : 0
                if (order > interceptors.size()) {
                    order = interceptors.size() // -1?
                }
                interceptors.add(order, interceptorAdapter)
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
                'display-name'("web-services")
                'servlet-name'("web-services")
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
