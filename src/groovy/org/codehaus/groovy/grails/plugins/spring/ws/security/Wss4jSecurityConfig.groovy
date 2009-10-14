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

package org.codehaus.groovy.grails.plugins.spring.ws.security

import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor
import org.springframework.ws.soap.security.wss4j.callback.SimplePasswordValidationCallbackHandler
import org.apache.ws.security.WSConstants

/**
 * Generates a Wss4jSecurityInterceptor from a WS-Security config class.
 *
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class Wss4jSecurityConfig {

    def securityConfigClass
    @Lazy Wss4jSecurityInterceptor interceptor = createInterceptor()

    // Spring Security integration
    def authenticationManager
    def userDetailsService
    def userCache
    def accessDecisionManager
    def objectDefinitionSource

    private createInterceptor() {
        assert securityConfigClass
        def interceptor = new Wss4jSecurityInterceptor()

        // validation
        if (securityConfigClass.metaClass.hasProperty(securityConfigClass, 'incomingMessageValidation')) {
            def validationVisitor = new Wss4jValidationVisitor(interceptor: interceptor, authenticationManager: authenticationManager,
                    userDetailsService: userDetailsService, userCache: userCache, accessDecisionManager: accessDecisionManager,
                    objectDefinitionSource: objectDefinitionSource)

            securityConfigClass.incomingMessageValidation.delegate = validationVisitor
            securityConfigClass.incomingMessageValidation()
            if (validationVisitor.validationActions) {
                interceptor.validationActions = validationVisitor.validationActions.join(' ')
            }
        }

        // securement
        if (securityConfigClass.metaClass.hasProperty(securityConfigClass, 'outgoingMessageSecurement')) {
            def securementVisitor = new Wss4jSecurementVisitor(interceptor: interceptor)
            securityConfigClass.outgoingMessageSecurement.delegate = securementVisitor
            securityConfigClass.outgoingMessageSecurement()

            if (securementVisitor.securementActions) {
                interceptor.securementActions = securementVisitor.securementActions.join(' ')
            }
        }
        return interceptor
    }
}

/**
 * Visitor for validation actions
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class Wss4jValidationVisitor {

    Wss4jSecurityInterceptor interceptor
    def validationActions = []

    def authenticationManager
    def userDetailsService
    def userCache
    def accessDecisionManager
    def objectDefinitionSource

    def propertyMissing(name, args) {
        //TODO support global keystore?
        switch (name) {
            case 'actor':
                interceptor.validationActor = args
                break
            default:
                throw new MissingPropertyException("Unknown property: $name")
        }
    }

    def usernameToken(params) {
        validationActions << 'UsernameToken'
        //TODO support Properties directly?
        if (params?.users && params.users instanceof Map) {
            def users = new Properties()
            params.users.each {
                users.setProperty(it.key.toString(), it.value.toString())
            }
            interceptor.validationCallbackHandler = new SimplePasswordValidationCallbackHandler(users: params.users)
        }
        else {
            if (params?.useSpringSecurity) {
                if (authenticationManager && userDetailsService && userCache && accessDecisionManager) {
                    interceptor.validationCallbackHandler = new Wss4jSpringSecurityPasswordCallbackHandler(
                            authenticationManager: authenticationManager, userDetailsService: userDetailsService,
                            userCache: userCache, accessDecisionManager: accessDecisionManager, objectDefinitionSource: objectDefinitionSource)
                }
                else {
                    throw new IllegalArgumentException('Spring Security beans not found. Make sure acegi plugin is installed.')
                }
            }
            else {
                throw new IllegalArgumentException('no authentication mechanism configured.')
            }
        }
    }


    def timestamp(params) {
        validationActions << 'Timestamp'
        if (params?.timeToLive) {
            interceptor.setTimeToLive(params.timeToLive)
        }
    }

    def signature(params) {
        validationActions << 'Signature'
        if (params?.keyStore) {
            interceptor.validationSignatureCrypto = params.keyStore
        }
//TODO handle unknown methods
    }
}



/**
 * Visitor for securement actions
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class Wss4jSecurementVisitor {

    Wss4jSecurityInterceptor interceptor
    def securementActions = []

    def propertyMissing(name, args) {
        //TODO support global keystore?
        switch (name) {
            case 'actor':
                interceptor.securementActor = args
                break
            case 'mustUnderstand':
                interceptor.securementMustUnderstand = args
                break
            default:
                throw new MissingPropertyException("Unknown property: $name")
        }
    }


    def usernameToken(params) {
        securementActions << 'UsernameToken'
        if (params?.username) {
            interceptor.securementUsername = params.username
        }
        if (params?.password) {
            interceptor.securementPassword = params.password
        }
        if (params?.passwordType) {
            switch (params.passwordType) {
                case 'Text':
                    interceptor.securementPasswordType = WSConstants.PW_TEXT
                    break
                case 'Digest':
                    interceptor.securementPasswordType = WSConstants.PW_DIGEST
                    break
                case 'None':
                    interceptor.securementPasswordType = WSConstants.PW_NONE
                    break
                default:
                    throw new IllegalArgumentException('invalid password type: ${params.passwordType}')
            }
        }

        def elements = []
        if (params?.nonce) {
            elements << 'Nonce'
        }
        if (params?.created) {
            elements << 'Created'
        }
        if (elements) {
            interceptor.securementUsernameTokenElements = elements.join(' ')
        }
    }

    def signature(params) {
        securementActions << 'Signature'
        if (params?.keyStore) {
            interceptor.securementSignatureCrypto = params.keyStore
        }
        if (params?.keyAlias) {
            interceptor.securementSignatureUser = params.keyAlias
        }
        if (params?.keyPassword) {
            interceptor.securementPassword = params.keyPassword
        }
        if (params?.keyReference) {
            switch (params.keyReference) {
                case 'Direct':
                    interceptor.securementSignatureKeyIdentifier = 'DirectReference'
                    break
                case 'IssuerSerial':
                    interceptor.securementSignatureKeyIdentifier = 'IssuerSerial'
                    break
                default: throw new IllegalArgumentException("invalid key reference value: ${params?.keyReference}")
            }
        }
        if (params?.signatureAlgorithm) {
            interceptor.securementSignatureAlgorithm = params.signatureAlgorithm
        }
        if (params?.signatureAlgorithm) {
            interceptor.securementSignatureAlgorithm = params.signatureAlgorithm
        }
        if (params?.parts) {
            // format parts to please wss4j
            def parts = params.parts.collect {
                if (it.startsWith('{')) {
                    "{}$it"
                } else it
            }
            interceptor.securementSignatureParts = parts.join(';')
        }
    }

    def timestamp(params) {
        securementActions << 'Timestamp'
        //TODO precision in milliseconds
    }
}
