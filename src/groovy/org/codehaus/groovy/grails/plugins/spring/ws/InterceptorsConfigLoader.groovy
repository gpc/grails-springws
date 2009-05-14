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

import org.codehaus.groovy.grails.plugins.spring.ws.InterceptorConfig

/**
 * Class that converts the closures of an endpoint interceptors class into  {@link InterceptorConfig}  instances
 *
 * @author Ivo Houbrechts (ivo@houbrechts-it.be)
 *
 */
public class InterceptorsConfigLoader {

    public static List getConfigs(Object interceptorsInstance) {

        if (!interceptorsInstance) return [];

        def loader = new InterceptorsConfigLoader(interceptorsInstance)
        def interceptorsClosure = interceptorsInstance.interceptors
        interceptorsClosure.delegate = loader
        interceptorsClosure.call()

        return loader.interceptorConfigs;
    }

    def interceptorsInstance
    def interceptorConfigs = []

    InterceptorsConfigLoader(interceptorsInstance) {
        this.interceptorsInstance = interceptorsInstance
    }

    def methodMissing(String methodName, args) {
        if (args) {
            def fc = new InterceptorConfig(name: methodName, interceptorsDefinition: interceptorsInstance)
            interceptorConfigs << fc

            if (args[0] instanceof Closure) {
                def closure = args[0]
                closure.delegate = fc
                closure.resolveStrategy = Closure.DELEGATE_FIRST
                closure.call()
            }
            else if (args[0] instanceof Map) {
                fc.scope = args[0].endpoint
                fc.interceptorList = args[0].interceptors ?: []
                if (args.size() > 1 && args[1] instanceof Closure) {
                    def closure = args[1]
                    closure.delegate = fc
                    closure.resolveStrategy = Closure.DELEGATE_FIRST
                    closure.call()
                }
            }
            fc.initialised = true
        }
    }
}