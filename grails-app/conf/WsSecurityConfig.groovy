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
 * A sample WS-Security definition class.
 *
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class WsSecurityConfig {

	// a key store defined in grails-app/conf/Config.groovy
    def myKeyStore

    // validation actions to apply to incoming messages
	def incomingMessageValidation = {
        usernameToken(users:['Gort':'Klaatu barada nikto'])
		timestamp()
   	}

    // securement actions to apply to outgoing messages
	def outgoingMessageSecurement = {
		mustUnderstand = true
		signature(keyStore: myKeyStore, keyAlias: 'mykey', keyPassword: '123456')
		timestamp()
    }
}
