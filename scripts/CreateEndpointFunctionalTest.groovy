includeTargets << grailsScript("Init")

target(main: "Creates a new Spring Web Services endpoint functional test") {
	typeName = "EndpointFunctional"
	depends( checkVersion, createEndpointFunctionalTest )
}

target (createEndpointFunctionalTest: "Implementation of create-endpoint-functional-test") {
	typeName <<= "Tests"
	artifactName = "EndpointFunctionalTests" 		
	artifactPath = "test/functional"
	createArtifact()
}

setDefaultTarget(main)
