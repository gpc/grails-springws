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
	
	void testSOAPDocumentService() {
		
		def response = withEndpointRequest(serviceURL) {
		 		HolidayRequest(xmlns: namespace) {
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
		assert status == "complete"
	}
	
	void testSOAPDocumentServiceValidationInterceptor() {

	  try {
     	 def response = withEndpointRequest(serviceURL) {
		 		HolidayRequest(xmlns: namespace) {
				     MyHoliday {
				       StartDate("2006-07-03")
				       EndDate("2006-07-07")
				     }
				     Me {
				       Number("42")
				       FirstName("Russ")
				       LastName("Miles")
				     }
				   }
				}
	     assert false
      }
	  catch (SoapFaultClientException e) {
		assert e.message == "Validation error"
		assert true
		return
	  }
	  assert false
    }
}