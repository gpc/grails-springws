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

/**
 * Configuration that drives the interceptors filter against 
 * each of the available endpoints.
 *
 * @author Ivo Houbrechts (ivo@houbrechts-it.be)
 */
class EndpointInterceptors {
   def loggingInterceptor
   def validatingInterceptor

   def interceptors = {
       stdout(endpoint:'*') {
           handleRequest = {messageContext, endpoint ->
              println "stdout:handleRequest"
              return true
           }

           handleResponse = {messageContext, endpoint ->
              println "stdout:handleResponse"
              return true
           }

           handleFault = {messageContext, endpoint ->
              println "stdout:handleFault"
              return true
           }
       }

       combined(endpoint: 'holiday', interceptors: [loggingInterceptor, validatingInterceptor]){
           handleRequest = {messageContext, endpoint ->
              println "combined:handleRequest, executed before loggingInterceptor.handleRequest and validatingInterceptor.handleRequest"
              return true
           }
       }

       never(endpoint: 'never'){
           handleRequest = {messageContext, endpoint ->
              throw new Exception("should never occur")
           }
       }

       always{
           handleResponse = {messageContext, endpoint ->
              println "always:handleResponse"
              return true
           }
       }
   }
}

