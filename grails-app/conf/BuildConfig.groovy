grails.project.work.dir = 'target'

grails.project.dependency.resolution = {
    inherits("global") 
    log "warn" 

    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
    }
    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        compile 'org.apache.ws.xmlschema:xmlschema-core:2.0.2'
        compile 'org.springframework.ws:spring-ws-core:2.2.0.RELEASE'
        compile ('org.springframework.ws:spring-ws-security:2.2.0.RELEASE'){
            excludes "xws-security", "wsit-rt", "spring-security-core"
        }
        compile ('org.springframework.security:spring-security-core:2.0.8.RELEASE') {
            //transitive = false
        }

        compile 'org.apache.ws.security:wss4j:1.6.10'
        test 'org.springframework:spring-expression:4.0.5.RELEASE'
        test 'wsdl4j:wsdl4j:1.6.2'
    }

    plugins {
        build(":release:3.0.1",
              ":rest-client-builder:1.0.3",
              ":tomcat:7.0.55",) {
            export = false
        }

        test ":functional-test:1.2.7"
    }
}
