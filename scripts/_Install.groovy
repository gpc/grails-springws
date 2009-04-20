
import groovy.xml.MarkupBuilder
                                                                
// Creating a web-services-servlet empty bean configuration file to satisfy MessageDispatcherServlet's built-in conventions
def webInfDir = "${basedir}/web-app/WEB-INF"

if (!new File(webInfDir, 'web-services-servlet.xml').exists()) {
    def writer = new FileWriter("${basedir}/web-app/WEB-INF/web-services-servlet.xml")
    def xml = new MarkupBuilder(writer)
    xml.beans(xmlns: 'http://www.springframework.org/schema/beans',
              'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
              'xsi:schemaLocation': 'http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd') {
    }
	
}
