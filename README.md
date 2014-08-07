**Spring WS Grails Plugin**

**Dependency:**  
`compile ':springws:2.0.0'`

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
