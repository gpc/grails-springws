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

package org.codehaus.groovy.grails.plugins.spring.ws.codec

import javax.xml.datatype.DatatypeFactory

/**
 * A base class for codecs encoding a Date or Calendar instance as XML date types.
 * Adjusts the time zone to UTC.
 * 
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */

abstract class AbstractXMLDateCodec {

    protected static DatatypeFactory datatypeFactory = DatatypeFactory.newInstance()

    protected static toCalendar(target) {
        GregorianCalendar calendar
        if (target instanceof GregorianCalendar) {
            calendar = target
        } else if (target instanceof Date) {
            calendar = new GregorianCalendar()
            calendar.time = target
        } else {
            throw new IllegalArgumentException('Codec only supports Dates and Calendars instances.')
        }
        calendar.timeZone = TimeZone.getTimeZone('UCT')
        return calendar
    }
}
