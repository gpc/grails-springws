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

package org.codehaus.groovy.grails.plugins.spring.ws.security

import java.io.IOException
import javax.security.auth.callback.UnsupportedCallbackException

import org.apache.ws.security.WSPasswordCallback
import org.apache.ws.security.WSUsernameTokenPrincipal
import org.apache.ws.security.WSSecurityException

import org.springframework.dao.DataAccessException
import org.springframework.security.*
import org.springframework.security.context.SecurityContextHolder
import org.springframework.security.intercept.ObjectDefinitionSource
import org.springframework.security.providers.UsernamePasswordAuthenticationToken
import org.springframework.security.providers.dao.UserCache
import org.springframework.security.providers.dao.cache.NullUserCache
import org.springframework.security.userdetails.UserDetails
import org.springframework.security.userdetails.UserDetailsService
import org.springframework.security.userdetails.UsernameNotFoundException
import org.springframework.ws.soap.security.callback.CleanupCallback
import org.springframework.ws.soap.security.wss4j.callback.AbstractWsPasswordCallbackHandler
import org.apache.commons.logging.LogFactory
import org.springframework.ws.soap.security.wss4j.callback.UsernameTokenPrincipalCallback
import org.codehaus.groovy.grails.web.util.WebUtils

/**
 * A Wss4J callback handler that intergates with Spring Security.
 * Supports authetication and authroization.
 *
 * @author Tareq Abedrabbo (tareq.abedrabbo@gmail.com)
 */
class Wss4jSpringSecurityPasswordCallbackHandler extends AbstractWsPasswordCallbackHandler {

    def log = LogFactory.getLog(Wss4jSpringSecurityPasswordCallbackHandler)

    UserCache userCache
    UserDetailsService userDetailsService
    AuthenticationManager authenticationManager
    AccessDecisionManager accessDecisionManager
    ObjectDefinitionSource objectDefinitionSource

    // handle plain text passwords
    protected void handleUsernameTokenUnknown(WSPasswordCallback callback)
    throws IOException, UnsupportedCallbackException {
        String identifier = callback.getIdentifier()
        Authentication authResult
        try {
            authResult = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, callback.password))
            log.debug "Authentication success: $authResult"
            SecurityContextHolder.context.authentication = authResult
        }
        catch (AuthenticationException failed) {
            log.debug "Authentication request for user '$identifier' failed: $failed"
            SecurityContextHolder.clearContext()
            throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION)
        }
        try {
            def request = WebUtils.retrieveGrailsWebRequest()
            def attributes = objectDefinitionSource.getAttributes(request)
            log.debug "attributes for $request= ${attributes?.dump()}"
            if(attributes) {
                accessDecisionManager.decide(authResult, request, objectDefinitionSource.getAttributes(request))
                log.debug "Authorization success for $authResult"
            }
        }
        catch (AccessDeniedException failed) {
            log.debug "Authorization request for user '$identifier' failed: $failed"
            SecurityContextHolder.clearContext()
            throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION)
        }
    }

    // handle digest passwords
    protected void handleUsernameToken(WSPasswordCallback callback) throws IOException, UnsupportedCallbackException {
        UserDetails user = loadUserDetails(callback.identifier)
        if (user && user.enabled && user.accountNonExpired && user.accountNonLocked && user.credentialsNonExpired) {
            callback.password = user.password
        }
    }

    protected void handleUsernameTokenPrincipal(UsernameTokenPrincipalCallback callback)
        throws IOException, UnsupportedCallbackException {
	
        UserDetails user = loadUserDetails(callback.principal.name)
        WSUsernameTokenPrincipal principal = callback.principal
        UsernamePasswordAuthenticationToken authRequest =
        new UsernamePasswordAuthenticationToken(principal, principal.password, user.authorities)
        log.debug "Authentication success: $authRequest"
        SecurityContextHolder.context.authentication = authRequest

    }

    private UserDetails loadUserDetails(String username) throws DataAccessException {
        UserDetails user = userCache.getUserFromCache(username)

        if (!user) {
            try {
                user = userDetailsService.loadUserByUsername(username)
            }
            catch (UsernameNotFoundException notFound) {
                log.debug "Username '$username' not found"
                return null
            }
            userCache.putUserInCache(user)
        }
        return user
    }

    protected void handleCleanup(CleanupCallback callback) throws IOException, UnsupportedCallbackException {
        SecurityContextHolder.clearContext()
    }
}
