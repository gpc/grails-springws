package org.codehaus.groovy.grails.plugins.spring.ws.security
import org.springframework.core.io.DefaultResourceLoader
/**
 *
 * @author tareq.abedrabbo
 */
class WsSecurityConfigFactory {

    static final CRYPTO_FACTORY_BEANS = {keyStore ->
        "${keyStore.key}"(org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean){ bean ->
            if(keyStore.value.location){
                keyStoreLocation = resourceLoader.getResource(keyStore.value.location)
            }
            if(keyStore.value.password){
                keyStorePassword = keyStore.value.password
            }
            if(keyStore.value.type){
                keyStoreType = keyStore.value.type
            }
            bean.singleton = true
            bean.autowire = "byName"
        }
    }

    static resourceLoader = new DefaultResourceLoader()

    static createInterceptor(params){
        def wss4j = new Wss4jSecurityConfig(params)
        return wss4j.interceptor
    }

    static createKeyStoreBean(keyStore){
        return CRYPTO_FACTORY_BEANS.curry(keyStore)
    }
}