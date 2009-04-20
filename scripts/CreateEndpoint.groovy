
includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsCreateArtifacts")

target(main: "Creates a new Spring Web Services endpoint") {
    depends(checkVersion, parseArguments)

    def type = "Endpoint"
    promptForName(type: type)
    
    def name = argsMap["params"][0]
   
	createArtifact(name: name, suffix: type, type: type, path: "grails-app/endpoints")			
}

setDefaultTarget(main)