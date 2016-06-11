**Spring WS Grails Plugin**

Spring WS plugin without security.

Grails 3
---

**build.gradle Dependency:**  

```groovy  
repositories {
    ...
    maven { url  "http://dl.bintray.com/dmahapatro/plugins" }
}

dependencies {
    ...
    compile 'org.grails.plugins:springws:3.0.0'
}
```

Plugin upgraded to Grails 3.1.8 with no security features. It uses `spring-boot-starter-ws` and partially follows the steps to [Produce a SOAP service in Spring Boot app](https://spring.io/guides/gs/producing-web-service/). Use of JAXB, `@Endpoint`, `@PayloadRoot`, `@RequestPayload` and `@ResponsePayload` is avoided in order to stick to legacy plugin architecture. 

> Legacy Grails 2 applications using this plugin and have a wide range of SOAP services will be stuck in Grails 2 unless services are moved to REST or a better version of SOAP Producer options. Making this plugin upgrade to Grails 3 will ease the transformation to Grails 3 apps without changing any of the plugin configuation or loginc inside the Endpoints and Interceptors.

Grails 2
---

With the upgrade of the plugin to Grails 2.4.2 came across the problem of clash between older version of spring security core with that of the latest got from Spring Security Core plugin 2.0-RC4.
This version is created by removing all security related classes and beans so that it won't give a compile error when used in an app in NetJets.

**Dependency:**  
`compile ':springws:2.1.0'`

**Feature for v2.1.0**
This is an update to the older version of plugin (v 2.0.0) with the below basic feature updates:

 - No Security features
 - No dependency to spring-security-core
 - Removed all security related classes to avoid compilation issues

**Feature for v2.0.0**
This is an update to the older version of plugin (v 1.0.0) with the below basic feature updates:

 - Updated to Grails 2.4.2
 - Updated `spring-ws-core` to version `2.2.0.RELEASE` from `2.1.2.RELEASE`.
 - Removed deprecated classes
 - Endpoints with security is a breaking change (TODO: Fix one test case)

**Feature for v1.0.0**  
This is an update to the older version of [springws](http://grails.org/plugin/springws) plugin (v 0.5.0) with the below basic feature updates:

 - Removed dependencies from `lib` directory to avoid clash with project dependencies.
 - Updated `spring-ws-core` to version `2.1.2` from `1.5.8`.
 - Added bean property `inline` to wsdl config in order to add imported schemas inline the `wsdl`.
 - Added a separate [sample app](https://github.com/dmahapatro/grails-springws-sample) showing the usage of latest version of `springws` plugin.

**To Run Locally**  
 - Clone the project.
 - Run `grails maven-install` to package the plugin and push it to local `.m2` repo if avaialble.
 - Or, use the plugin location directly using `grails.plugin.location.springws="your/plugin/location"`.
