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

import org.codehaus.groovy.grails.plugins.spring.ws.EndpointFunctionalTestCase

import org.springframework.ws.soap.client.SoapFaultClientException
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.ws.soap.security.wss4j.support.CryptoFactoryBean

/**
  * An example of testing a secured endpoint
  *
  * @author Russ Miles (russ@russmiles.com)
  * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
  *
  */
class CancelHolidayEndpointFunctionalTests extends EndpointFunctionalTestCase {

	def serviceURL = "http://localhost:8080/springws/services"

    def namespace = "http://mycompany.com/hr/schemas"

    def resourceLoader = new DefaultResourceLoader()
    def keyStore

    void setUp(){
      super.setUp()
      webServiceTemplate.setDefaultUri(serviceURL)
      def factory = new CryptoFactoryBean()
      factory.keyStoreLocation = resourceLoader.getResource('file:grails-app/keys/mykeystore.jks')
      factory.keyStorePassword = '123456'
      factory.afterPropertiesSet()
      keyStore = factory.getObject()
    }

    void testSOAPDocumentService() {

      def security = new ClientWsSecurityConfig(keyStore: keyStore)

      def response = withSecuredEndpointRequest(serviceURL, security) {
               CancelHolidayRequest(xmlns: namespace) {
                   Holiday {
                     StartDate("2006-07-03")
                     EndDate("2006-07-07")
                   }
                   Employee {
                     Number("42")
                     FirstName("Russ")
                     LastName("Miles")
                   }
                 }
              }

      def status = response.status
      assert status == 'canceled'
    }
}

class ClientWsSecurityConfig {

    def keyStore

    def outgoingMessageSecurement = {
        usernameToken(username:'Gort', password:'Klaatu barada nikto')
        timestamp()
    }

    def incomingMessageValidation = {
        signature(keyStore: keyStore)
        timestamp()
    }
}
