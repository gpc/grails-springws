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

import org.codehaus.groovy.grails.plugins.spring.ws.codec.AbstractXMLDateCodec


/**
 * Encoder for XML date type.
 *
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class XMLDateCodec extends AbstractXMLDateCodec {

    static encode = {target ->
        def calendar = toCalendar(target)
        def xmlCalendar = datatypeFactory.newXMLGregorianCalendar()
        xmlCalendar.year = calendar.get(Calendar.YEAR)
        xmlCalendar.month = calendar.get(Calendar.MONTH)
        xmlCalendar.day = calendar.get(Calendar.DAY_OF_MONTH)
        xmlCalendar.toXMLFormat()
    }
}
