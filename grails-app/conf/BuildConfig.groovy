grails.project.work.dir = 'target'

grails.project.dependency.resolution = {
    inherits "global"
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

        //Classifiers cannot be used because of JIRA GRAILS-6147
        //compile group:'org.springframework.ws', name:'spring-ws', version:'2.1.2.RELEASE', classifier:'all'

        compile 'org.apache.ws.xmlschema:xmlschema-core:2.0.2'
        compile 'org.springframework.ws:spring-ws-core:2.1.2.RELEASE'
        compile ('org.springframework.ws:spring-ws-security:2.1.2.RELEASE'){
            excludes "xws-security", "wsit-rt", "spring-security-core"
        }
        compile 'org.springframework.security:spring-security-core:2.0.6.RELEASE'
        compile 'org.apache.ws.security:wss4j:1.6.10'
    }
    plugins {
        build ":release:2.2.1", ":rest-client-builder:1.0.3", {
            export = false
        }
    }
}
