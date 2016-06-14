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

package org.codehaus.groovy.grails.plugins.spring.ws;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;

import javax.xml.transform.Source;
import java.io.Writer;

import org.springframework.xml.transform.StringSource;

/**
 * Abstract class for Spring Web Services endpoint artefacts.
 * This class provides the hooks for different request and response type strategies 
 *
 * @author Russ Miles (russ@russmiles.com)
 *
 */
public abstract class AbstractGrailsEndpointClass extends AbstractInjectableGrailsClass implements GrailsEndpointClass {

	public static final String INVOKE = "invoke";

	public AbstractGrailsEndpointClass(Class clazz, String artefactIdentifier) {
		super(clazz, artefactIdentifier);
	}

	public Source invoke(Source request) throws Exception {
        Writer responseWriter = createResponseWriter();
        getMetaClass().invokeMethod( getReferenceInstance(), INVOKE, new Object[] {
                createRequest(request),
                createResponse(responseWriter)
        } );
        return new StringSource(responseWriter.toString());
	}

    protected abstract Object createRequest(Source request) throws Exception;

    protected abstract Writer createResponseWriter();

    protected abstract Object createResponse(Writer writer);
}

