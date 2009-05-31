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

package org.codehaus.groovy.grails.plugins.spring.ws

import groovy.xml.MarkupBuilder
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import org.codehaus.groovy.grails.plugins.spring.ws.AbstractEndpointAdapter
import org.springframework.xml.transform.StringResult

/**
 * Default endpoint adapter that transforms the request into an 
 * {@link XmlSlurper} and the response into a {@link MarkupBuilder}
 *
 * @author Russ Miles (russ@russmiles.com)
 * @author Ivo Houbrechts (ivo@houbrechts-it.be)
 * @author Okke Tijhuis (o.tijhuis@gmail.com)
 *
 */
public class DefaultEndpointAdapter extends AbstractEndpointAdapter {
    private final TransformerFactory transformerFactory

    public DefaultEndpointAdapter() {
        this.transformerFactory = TransformerFactory.newInstance();
    }

    protected Object createRequest(Source request) throws Exception {
		def transformer = transformerFactory.newTransformer()
        StringResult result = new StringResult()
        transformer.transform(request, result)
        return new XmlSlurper().parseText(result.toString())
    }

    protected Writer createResponseWriter() {
        return new StringWriter()
    }

    protected Object createResponse(Writer writer) {
        return new MarkupBuilder(writer)
    }
}