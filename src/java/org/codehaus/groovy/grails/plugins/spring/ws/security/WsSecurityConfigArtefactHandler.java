/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.codehaus.groovy.grails.plugins.spring.ws.security;

import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
import static org.codehaus.groovy.grails.plugins.spring.ws.security.DefaultGrailsWsSecurityConfigClass.WS_SECURITY_CONFIG;

/**
 *
 * @author tareq.abedrabbo
 */
public class WsSecurityConfigArtefactHandler extends ArtefactHandlerAdapter{

    public WsSecurityConfigArtefactHandler(){
        super(WS_SECURITY_CONFIG,GrailsWsSecurityConfigClass.class, DefaultGrailsWsSecurityConfigClass.class, WS_SECURITY_CONFIG);
    }

}
