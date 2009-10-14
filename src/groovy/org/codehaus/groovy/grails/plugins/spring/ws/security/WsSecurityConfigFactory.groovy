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

import org.springframework.core.io.DefaultResourceLoader

/**
 * A factory class for WS-Security configs
 *
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class WsSecurityConfigFactory {

    static final CRYPTO_FACTORY_BEANS = {keyStore ->
        "${keyStore.key}"(org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean) { bean ->
            if(keyStore.value.location) {
                keyStoreLocation = resourceLoader.getResource(keyStore.value.location)
            }
            if(keyStore.value.password) {
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

    static createInterceptor(params) {
        def wss4j = new Wss4jSecurityConfig(params)
        return wss4j.interceptor
    }

    static createKeyStoreBean(keyStore) {
        return CRYPTO_FACTORY_BEANS.curry(keyStore)
    }
}
