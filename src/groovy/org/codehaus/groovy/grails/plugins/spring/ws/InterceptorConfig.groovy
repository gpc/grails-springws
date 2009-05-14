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

import org.codehaus.groovy.grails.commons.GrailsClassUtils

/**
 * Class that holds the configuration details of an endpoint interceptor artifact
 *
 * @author Ivo Houbrechts (ivo@houbrechts-it.be)
 *
 */
public class InterceptorConfig {
    String name                                           \
    /**
     * Regular expression for selecting affected endpoints
     */
    String scope
    /**
     * List of predefined {@link org.springframework.ws.server.EndpointInterceptor} instances
     */
    List interceptorList
    Closure handleRequest
    Closure handleResponse
    Closure handleFault
    boolean initialised = false

    /**
     * This is the interceptors definition bean that declared the interceptor
     * config. Since it may contain injected services, etc., we
     * delegate any missing properties or methods to it.
     */
    def interceptorsDefinition

    /**
     * When the interceptor does not have a particular property, it passes
     * the request on to the interceptor definition class.
     */
    def propertyMissing(String propertyName) {
        // Delegate to the parent definition if it has this property.
        if (this.interceptorsDefinition.metaClass.hasProperty(this.interceptorsDefinition, propertyName)) {
            def getterName = GrailsClassUtils.getGetterName(propertyName)
            InterceptorConfig.metaClass."$getterName" = {-> this.interceptorsDefinition."$propertyName" }
            return this.interceptorsDefinition."$propertyName"
        }
        else {
            throw new MissingPropertyException(propertyName, this.interceptorsDefinition.getClass())
        }
    }

    /**
     * When the interceptor does not have a particular method, it passes
     * the call on to the interceptor definition class.
     */
    def methodMissing(String methodName, args) {
        // Delegate to the parent definition if it has this method.
        if (this.interceptorsDefinition.metaClass.respondsTo(this.interceptorsDefinition, methodName)) {
            if (!args) {
                // No argument method.
                InterceptorConfig.metaClass."$methodName" = {->
                    return this.interceptorsDefinition."$methodName"()
                }
            }
            else {
                InterceptorConfig.metaClass."$methodName" = {varArgs ->
                    return this.interceptorsDefinition."$methodName"(varArgs)
                }
            }

            // We've created the forwarding method now, but we still
            // need to invoke the target method this time around.
            return this.interceptorsDefinition."$methodName"(* args)
        }
        else {
            // Ideally, we would throw a MissingMethodException here
            // whether the interceptor config is intialised or not. However,
            // if it's in the initialisation phase, the MME gets
            // swallowed somewhere.
            if (!initialised) {
                throw new IllegalStateException(
                        "Invalid interceptor definition in ${this.interceptorsDefinition.getClass().name} - trying "
                                + "to call method '${methodName}' outside of an interceptor.")
            }
            else {
                // The required method was not found on the parent interceptor
                // definition either.
                throw new MissingMethodException(methodName, this.interceptorsDefinition.getClass(), args)
            }
        }
    }

    public String toString() {"InterceptorConfig[$name, scope=$scope, interceptorList: $interceptorList]"}
}