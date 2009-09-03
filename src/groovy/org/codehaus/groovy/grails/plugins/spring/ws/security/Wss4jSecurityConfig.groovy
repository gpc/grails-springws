/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codehaus.groovy.grails.plugins.spring.ws.security

import org.springframework.ws.soap.security.wss4j.Wss4jSecurityInterceptor
import org.springframework.ws.soap.security.wss4j.callback.SimplePasswordValidationCallbackHandler
import org.apache.ws.security.components.crypto.Crypto

/**
 *
 * @author tareq.abedrabbo
 */
class Wss4jSecurityConfig {

    def securityConfigClass
    @Lazy Wss4jSecurityInterceptor interceptor = createInterceptor()
    def validationActions = ''

    // Spring Security integration
    def authenticationManager
    def userDetailsService
    def userCache
    def accessDecisionManager
    def objectDefinitionSource

    private createInterceptor(){
        assert securityConfigClass
        def interceptor = new Wss4jSecurityInterceptor()
        //TODO check whether securement and validation action exist

        // validation
        def validationVisitor = new Wss4jValidationVisitor(interceptor:interceptor, authenticationManager:authenticationManager, 
            userDetailsService:userDetailsService, userCache:userCache, accessDecisionManager:accessDecisionManager,
            objectDefinitionSource:objectDefinitionSource)
        securityConfigClass.validation.delegate = validationVisitor
        securityConfigClass.validation()
        if(validationVisitor.validationActions){
            interceptor.validationActions = validationVisitor.validationActions.join(' ')
        }

        // securement
        def securementVisitor = new Wss4jSecurementVisitor(interceptor:interceptor)
        securityConfigClass.securement.delegate = securementVisitor
        securityConfigClass.securement()

        if(securementVisitor.securementActions){
            interceptor.securementActions = securementVisitor.securementActions.join(' ')
        }
        return interceptor
    }
}

class Wss4jValidationVisitor {

    Wss4jSecurityInterceptor interceptor
    def validationActions = []

    def authenticationManager
    def userDetailsService
    def userCache
    def accessDecisionManager
    def objectDefinitionSource

    def propertyMissing(name, args){
        // support global keystore
        switch(name){
            case 'actor':
            interceptor.validationActor = args
            break
        }
    }

    //    Keep this one or the property based config?
    def actor(value){
        interceptor.validationActor = value
    }

    def actions(a){
        a.delegate = this
        a.call()
    }

    def usernameToken(params){
        validationActions << 'UsernameToken'
        //TODO support Properties directly
        //TODO support spring security
        if(params?.users && params.users instanceof Map){
            def users = new Properties()
            params.users.each{
                users.setProperty(it.key.toString(), it.value.toString())
            }
            interceptor.validationCallbackHandler = new SimplePasswordValidationCallbackHandler(users:params.users)
        }else{
            if(authenticationManager && userDetailsService && userCache && accessDecisionManager){
                interceptor.validationCallbackHandler = new Wss4jSpringSecurityPasswordCallbackHandler(
                    authenticationManager:authenticationManager, userDetailsService:userDetailsService, 
                    userCache:userCache, accessDecisionManager:accessDecisionManager, objectDefinitionSource:objectDefinitionSource)
            }
            else{
                //TODO better message. suggest usersmap or spring security
                throw new IllegalArgumentException('no authentication mechanism configured.')
            }
        }
    }
    
    def timestamp(params){
        validationActions << 'Timestamp'
    }

    //TODO handle unknown methods
}

class Wss4jSecurementVisitor {

    Wss4jSecurityInterceptor interceptor
    def securementActions = []

    def actions(a){
        a.delegate = this
        a.call()
    }

    def signature(params){
        println "signature params ${params.inspect()}"
        securementActions << 'Signature'
        if(params.keyStore && params.keyStore instanceof Crypto){
            interceptor.securementSignatureCrypto = params.keyStore
        }
        if(params.keyAlias){
            interceptor.securementUsername = params.keyAlias
        }
        if(params.keyPassword){
            interceptor.securementPassword = params.keyPassword
        }
    }
}
