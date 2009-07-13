/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/**
 * A simple endpoint that overrides the default Endpoint name based
 * naming strategy for incoming documents
 *
 * @author Russ Miles (russ@russmiles.com)
 */
class CustomRequestElementEndpoint {
	
	def static namespace = "http://mycompany.com/hr/schemas" 
	
	// Rather than using the default strategy, which 
	// would map incoming documents with a root payload of
	// 'CustomRequestElement' elements according to the endpoint name,
	// we explicitly specify that we're interested in the payload being
	// a 'Vacation' element, in the above indicated namespace.
	def static requestElement= "Vacation"

	def invoke = { request, response ->

	  // Using the incoming document
	  println "Vacation Request Received!"
      println "Start Date: ${request.Holiday.StartDate}"
      println "End Date: ${request.Holiday.EndDate}"

      // Typically you'd invoke some internal business services here

      // Preparing the response document
	  response.VacationResponse(xmlns: namespace) {
         status('complete') {
         }
      }
      
    }
}