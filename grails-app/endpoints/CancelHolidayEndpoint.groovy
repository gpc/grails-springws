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
 * A simple sample endpoint for testing purposes
 *
 * @author Russ Miles (russ@russmiles.com)
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class CancelHolidayEndpoint {
	
	def static namespace = "http://mycompany.com/hr/schemas"

    def static wsSecurity = true

	def invoke = { request, response ->

	  // Using the incoming document
	  println "Holiday Request Received!"
      println "Start Date: ${request.Holiday.StartDate}"
      println "End Date: ${request.Holiday.EndDate}"

      // Typically you'd invoke some internal business services here

      // Preparing the response document
	  response.HolidayResponse(xmlns: namespace) {
         status('canceled') {
         }
      }
    }
}
