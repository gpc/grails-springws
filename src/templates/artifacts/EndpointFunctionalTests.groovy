@artifact.package@

import org.codehaus.groovy.grails.plugins.spring.ws.EndpointFunctionalTestCase

class @artifact.name@ extends EndpointFunctionalTestCase {
	
	def serviceURL = "http://localhost:8080/your-app/services"

    def namespace = "http://mycompany.com/your-org/schemas"

    void setUp(){
      super.setUp()
      webServiceTemplate.setDefaultUri(serviceURL)
    }
    
    void testSOAPDocumentService() {
       assert false
    }
}