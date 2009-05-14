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
import groovy.xml.MarkupBuilder

import org.springframework.ws.soap.client.SoapFaultClientException

 /**
  * An example of testing the Holiday endpoint and applied interceptors
  * with the full web service stack in place.
  *
  * @author Russ Miles (russ@russmiles.com)
  *
  */
class HolidayEndpointFunctionalTests extends EndpointFunctionalTestCase {

	def serviceURL = "http://localhost:8080/springws/services"

    def namespace = "http://mycompany.com/hr/schemas"

    void setUp(){
      super.setUp()
      webServiceTemplate.setDefaultUri(serviceURL)
    }

    void testSOAPDocumentService() {
      def request = this.createStringRequest()
      def response = webServiceTemplate.sendToEndpoint(request)
      println(response)
      def holidayResponse = new XmlSlurper().parseText(response)
      def status = holidayResponse.status
      assert status == "complete"
    }

	void testSOAPDocumentServiceValidationInterceptor() {
      def request = this.createIncorrectStringRequest()
	  try {
     	 def response = webServiceTemplate.sendToEndpoint(request)
	     println(response)
	     assert false
      }
	  catch (SoapFaultClientException e) {
		assert e.message == "Validation error"
		assert true
		return
	  }
	  assert false
    }

    String createStringRequest(){
      def writer = new StringWriter()
      def request = new MarkupBuilder(writer)
	  request.HolidayRequest(xmlns: namespace) {
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
      writer.toString()
    }

	String createIncorrectStringRequest(){
      def writer = new StringWriter()
      def request = new MarkupBuilder(writer)
	  request.HolidayRequest(xmlns: namespace) {
         MyHoliday {
           StartDate("2006-07-03")
           EndDate("2006-07-07")
         }
         Employee {
           Number("42")
           FirstName("Russ")
           LastName("Miles")
         }
      }
      writer.toString()
    }
}