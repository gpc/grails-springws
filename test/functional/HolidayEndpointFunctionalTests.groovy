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
  * @author Okke Tijhuis (o.tijhuis@gmail.com)
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
	
	def static results = []

    /*
	 * Added test to cause a concurrency bug in DefaultEndpointAdapter discovered in version 0.2. 
	 * This causes a fatal thread based error on the server without the fix.
	 */
    void testMultipleConcurrentSOAPDocumentServiceInvocations() {
        def threads = []
        (1..10).each {
            threads.add(Thread.start {
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
                addResult(response.status)
            })
        }
        // Following is needed since jUnit doesn't wait for the threads to finish 
		// so you can't see any output from the threads otherwise
        threads.each {
            it.join()
        }
		println results
        assert results == ["complete","complete","complete","complete","complete","complete","complete","complete","complete","complete"]
    }

	synchronized addResult(result) {
        results.add(result)
    }
	
	/*
	 * Test the incoming payload document validation interceptor
	 *
	 */
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

	/*
	 * Test the custom request payload element configuration option
	 *
	 */
	void testSOAPDocumentServiceCustomPayloadElement() {
		
		def response = withEndpointRequest(serviceURL) {
		 		Vacation(xmlns: namespace) {
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
}