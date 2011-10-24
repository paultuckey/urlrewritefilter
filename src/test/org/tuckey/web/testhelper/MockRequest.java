/**
 * Copyright (c) 2005-2007, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.tuckey.web.testhelper;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.CharArrayReader;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

/**
 * @author Paul Tuckey
 * @version $Revision: 36 $ $Date: 2006-09-19 18:32:39 +1200 (Tue, 19 Sep 2006) $
 */
public class MockRequest implements HttpServletRequest {

    private String requestURI;
    private int serverPort = 80;
    private String queryString;
    private String method = "GET";
    private Hashtable headers = new Hashtable();
    private Hashtable attrs = new Hashtable();
    private Hashtable parameters = new Hashtable();
    private MockSession session = null;
    private String authType;
    private int contentLength;
    private String contentType;
    private String contextPath = "";
    private Cookie[] cookies;
    private int cookieCounter;
    private String pathInfo;
    private String pathTranslated;
    private String protocol;
    private String remoteAddr;
    private String remoteHost;
    private String remoteUser;
    private String requestedSessionId;
    private boolean requestedSessionIdFromCookie = false;
    private boolean requestedSessionIdFromURL = false;
    private boolean requestedSessionIdValid = false;
    private String requestUrl;
    private String serverName;
    private String servletPath;
    private String scheme;
    private int localPort = 0;
    private String body;

    public MockRequest() {
    }

    public MockRequest(String requestURI) {
        this.requestURI = contextPath + requestURI;
        this.servletPath = requestURI;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String s) {
        authType = s;
    }


    public Cookie[] getCookies() {
        return cookies;
    }

    public long getDateHeader(String s) {
        return 0;
    }

    public String getHeader(String s) {
        if (s == null) {
            return null;
        }
        return (String) headers.get(s);
    }

    public Enumeration getHeaders(String s) {
        return headers.elements();
    }

    public Enumeration getHeaderNames() {
        return headers.keys();
    }

    public int getIntHeader(String s) {
        return 0;
    }

    public String getMethod() {
        return method;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getPathTranslated() {
        return pathTranslated;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getQueryString() {
        return queryString;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    private ArrayList roles = new ArrayList();

    public boolean isUserInRole(String s) {
        return roles.contains(s);
    }

    public void addRole(String s) {
        roles.add(s);
    }

    public void removeRole(String s) {
        roles.remove(s);
    }


    public Principal getUserPrincipal() {
        return null;
    }

    public String getRequestedSessionId() {
        return requestedSessionId;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public StringBuffer getRequestURL() {
        if (requestUrl == null) return null;
        return new StringBuffer(requestUrl);
    }

    public String getServletPath() {
        return servletPath;
    }

    public HttpSession getSession(boolean b) {
        if (b && session == null) session = new MockSession();
        return session;
    }

    public void setSessionNew(boolean b) {
        if (session == null) session = new MockSession();
        session.setNew(b);
    }

    public HttpSession getSession() {
        return session;
    }

    public MockSession getMockSession() {
        return session;
    }
    
    public boolean isRequestedSessionIdValid() {
        return requestedSessionIdValid;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return requestedSessionIdFromCookie;
    }

    public boolean isRequestedSessionIdFromURL() {
        return requestedSessionIdFromURL;
    }

    /**
     * @deprecated
     */
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public Object getAttribute(String s) {
        return attrs.get(s);
    }

    public Enumeration getAttributeNames() {
        return null;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    String characterEncoding;

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        characterEncoding = s;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    public String getParameter(String s) {
        return (String) parameters.get(s);
    }

    public void setParameter(String s, String v) {
        parameters.put(s, v);
    }

    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    public String[] getParameterValues(String s) {
        return new String[0];
    }

    public Map getParameterMap() {
        return null;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new CharArrayReader(body.toCharArray()));
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setAttribute(String s, Object o) {
        attrs.put(s, o);
    }

    public void removeAttribute(String s) {
        attrs.remove(s);
    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration getLocales() {
        return null;
    }

    public boolean isSecure() {
        return false;
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return new MockRequestDispatcher(s);
    }

    /**
     * @deprecated
     */
    public String getRealPath(String s) {
        return null;
    }

    public int getRemotePort() {
        return 0;
    }

    public String getLocalName() {
        return null;
    }

    public String getLocalAddr() {
        return null;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setServerPort(int i) {
        serverPort = i;
    }

    public void setQueryString(String s) {
        queryString = s;
    }

    public void setMethod(String s) {
        method = s;
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setContentLength(int i) {
        contentLength = i;
    }

    public void setContentType(String s) {
        contentType = s;
    }

    public void setContextPath(String s) {
        contextPath = s;
    }

    public void addCookie(Cookie c) {
        if (cookies == null) cookies = new Cookie[100];
        cookies[cookieCounter++] = c;
    }

    public void addParameter(String s, String s1) {
        parameters.put(s, s1);
    }

    public void setPathInfo(String s) {
        pathInfo = s;
    }

    public void setPathTranslated(String s) {
        pathTranslated = s;
    }

    public void setProtocol(String s) {
        protocol = s;
    }

    public void setRemoteAddr(String s) {
        remoteAddr = s;
    }

    public void setRemoteHost(String s) {
        remoteHost = s;
    }

    public void setRemoteUser(String s) {
        remoteUser = s;
    }

    public void setRequestedSessionId(String s) {
        requestedSessionId = s;
    }

    public void setRequestURL(String s) {
        requestUrl = s;
    }

    public void setServerName(String s) {
        serverName = s;
    }

    public void setScheme(String s) {
        scheme = s;
    }

    public void addHeader(String s, String s1) {
        headers.put(s, s1);
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public void setBody(String s) {
        body = s;
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }

    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }
}
