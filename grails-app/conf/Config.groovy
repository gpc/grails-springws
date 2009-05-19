// An example of exporting the WSDL, in the simplest case, for the Holiday Endpoint
springws.wsdl.Holiday.export

// A more complex wsdl export could take the form of:
//springws {
//    wsdl {
//        Holiday {
//            wsdlName= 'Holiday-v2'
//            xsds= '/hr/v2/schemas/Holiday.xsd'
//            portTypeName = 'HolidayPort'
//            serviceName = 'HolidayService'
//            locationUri = grails.serverURL + '/services/hr/v2/Holiday'
//            targetNamespace = 'http://mycompany.com/hr/v2/definitions'
//        }
//    }
//}

// log4j configuration
log4j = {
    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	       'org.codehaus.groovy.grails.web.pages', //  GSP
	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails."web.mapping.filter', // URL mapping
	       'org.codehaus.groovy.grails."web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	       'org.springframework',
	       'org.hibernate'

    debug 'SpringwsGrailsPlugin'
}

grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
