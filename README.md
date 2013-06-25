**Spring WS Grails Plugin**

This is an update to the older version of the [springws](http://grails.org/plugin/springws) plugin (v 0.5.0) with the below basic feature updates:

 - Removed dependencies from `lib` directory to avoid clash with project dependencies.
 - Updated `spring-ws-core` to version `2.1.2` from `1.5.8`.
 - Added bean property `inline` to wsdl config in order to add imported schemas inline in the `wsdl`.
 - Added a separate [sample app](https://github.com/dmahapatro/grails-springws-sample) showing the full use of latest version of `springws` plugin.

**To Run Locally**  
 - Clone the project.
 - Run `grails maven-install` to package the plugin and push it to local `.m2` repo if avaialble.
 - Or, use the plugin location directly using `grails.plugin.location.springws="your/plugin/location"`
 - Very soon I would submit a pull request to the master branch once I am done with few cosmetic changes.
