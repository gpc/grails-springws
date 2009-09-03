/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codehaus.groovy.grails.plugins.spring.ws.security;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;

/**
 *
 * @author tareq.abedrabbo
 */
public class DefaultGrailsWsSecurityConfigClass extends AbstractInjectableGrailsClass implements GrailsWsSecurityConfigClass  {

    public static final String WS_SECURITY_CONFIG = "WsSecurityConfig";

    public DefaultGrailsWsSecurityConfigClass(Class clazz){
        super(clazz, WS_SECURITY_CONFIG);
    }

}
