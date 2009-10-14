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

package org.codehaus.groovy.grails.plugins.spring.ws.fault

import org.springframework.ws.soap.server.endpoint.AbstractSoapFaultDefinitionExceptionResolver
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition
import org.springframework.ws.soap.SoapFault
import static org.codehaus.groovy.grails.commons.GrailsClassUtils.isStaticProperty
import static org.springframework.ws.soap.server.endpoint.annotation.FaultCode.*
import javax.xml.namespace.QName
import org.springframework.xml.transform.StringSource
import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer

/**
 * Maps an exception to a fault. Still an early prototype.
 *
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
public class DomainSoapFaultExceptionResolver extends AbstractSoapFaultDefinitionExceptionResolver {

    static FAULT_STRING = 'faultString'
    static FAULT_REASON = 'faultReason'
    static THROWABLE_GETTERS = Throwable.methods*.name.findAll {it.startsWith('get')}

    protected SoapFaultDefinition getFaultDefinition(Object o, Exception e) {

        if (!isStaticProperty(e.class, 'faultCode')) {
            return null
        }

        // set fault code
        def definition = new SoapFaultDefinition()
        if (e.faultCode instanceof String) {
            switch (e.faultCode.toUpperCase()) {
                case 'CLIENT':
                    definition.faultCode = CLIENT.value()
                    break
                case 'RECEIVER':
                    definition.faultCode = RECEIVER.value()
                    break
                case 'SENDER':
                    definition.faultCode = SENDER.value()
                    break
                case 'SERVER':
                    definition.faultCode = SERVER.value()
                    break
                case 'CUSTOM':
                    definition.faultCode = CUSTOM.value()
                    break
                default:
                    throw new IllegalArgumentException("invalid fault code: ${e.faultCode}")
            }
        }
        else {
            definition.faultCode = e.faultCode.value()
        }

        // set fault string
        if (isStaticProperty(e.class, FAULT_STRING) && isStaticProperty(e.class, FAULT_REASON)) {
            throw new IllegalArgumentException("Both [$FAULT_STRING] and [$FAULT_REASON] are set. Set only one of these properties.")
        }
        if (isStaticProperty(e.class, FAULT_STRING)) {
            definition.faultStringOrReason = e."$FAULT_STRING"
        } else if (isStaticProperty(e.class, 'faultReason')) {
            definition.faultStringOrReason = e.faultReason
        } else {
            definition.faultStringOrReason = e.toString()
        }
        return definition
    }

    protected void customizeFault(Object o, Exception e, SoapFault fault) {
//        def elements = e.class.methods*.name.findAll {it.startsWith('get')} - THROWABLE_GETTERS
        def detail = fault.addFaultDetail()
        def xml = e.encodeAsXML()
        StringSource source = new StringSource(xml)
        TransformerFactory factory = TransformerFactory.newInstance()
        Transformer transformer = factory.newTransformer()
        def result = detail.getResult()
        transformer.transform(source, result)
/*        elements.each {
            def name = new QName('http://test', it)
            def element = detail.addFaultDetailElement(name)
            element.addText(e."$it"().encodeAsXML())
        }*/
    }
}
