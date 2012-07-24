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
package org.tuckey.web.filters.urlrewrite.test;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.NormalRewrittenUrl;
import org.tuckey.web.filters.urlrewrite.Rule;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The idea for UrlRewriteTestCase is that users can extend it to create their own
 * simple unit tests instead of having to manually setup a mock request and the filter.
 * <p/>
 * note, Ideally this would be in a separate urlrewrite-test.jar but that
 * seems a little over the top for one class.
 */
public class UrlRewriteTestCase extends TestCase {

    Conf conf;
    UrlRewriter urlRewriter;

    public void loadConf(URL confFileUrl) {
        Log.setLevel("SYSOUT:DEBUG");
        conf = new Conf(confFileUrl);
        assertTrue("Conf should load without errors", conf.isOk());
        urlRewriter = new UrlRewriter(conf);
    }

    public Rule getRule(String ruleName) {
        List rules = urlRewriter.getConf().getRules();
        Rule rule = null;
        if (rules != null) {
            for (int i = 0; i < rules.size(); i++) {
                Rule loopRule = (Rule) rules.get(i);
                if (ruleName.equalsIgnoreCase(loopRule.getName())) {
                    rule = loopRule;
                }
            }
        }
        if (rule == null) {
            assertTrue("Rule by the name " + ruleName + " does not exist", false);
        }
        return rule;
    }

    /**
     * Checks to see if the specified rule name matches the url specified.
     *
     * @param ruleName   - the name of the rule
     * @param requestUrl - the url to check
     */
    public void assertRuleMatches(String ruleName, String requestUrl) {

        Rule rule = getRule(ruleName);

        MockResponse response = new MockResponse();
        MockRequest request = new MockRequest(requestUrl);
        NormalRewrittenUrl rewrittenUrl = null;
        try {
            rewrittenUrl = (NormalRewrittenUrl) rule.matches(request.getRequestURI(), request, response);

        } catch (IOException e) {
            assertNull("IOException during rule matching " + e.toString(), e);

        } catch (ServletException e) {
            assertNull("ServletException during rule matching " + e.toString(), e);

        } catch (InvocationTargetException e) {
            assertNull("InvocationTargetException during rule matching " + e.toString(), e);

        }
        assertNotNull("Rule " + ruleName + " does not match", rewrittenUrl);
    }

    public void assertRuleDoesNotMatches(String ruleName, String requestUrl) {
        Rule rule = getRule(ruleName);

        MockResponse response = new MockResponse();
        MockRequest request = new MockRequest(requestUrl);
        NormalRewrittenUrl rewrittenUrl = null;
        try {
            rewrittenUrl = (NormalRewrittenUrl) rule.matches(request.getRequestURI(), request, response);

        } catch (IOException e) {
            assertNull("IOException during rule matching " + e.toString(), e);

        } catch (ServletException e) {
            assertNull("ServletException during rule matching " + e.toString(), e);

        } catch (InvocationTargetException e) {
            assertNull("InvocationTargetException during rule matching " + e.toString(), e);

        }
        assertNull("Rule " + ruleName + " match", rewrittenUrl);
    }


    /**
     * An empty method so that Junit doesn't complain when running tests.
     */
    public void testUrlRerwriteTestCase() {
        // do nothing
    }
}


class MockRequest implements HttpServletRequest {

    private String requestURI;
    private int serverPort = 80;
    private String queryString;
    private String method = "GET";
    private Hashtable headers = new Hashtable();
    private Hashtable attrs = new Hashtable();
    private Hashtable parameters = new Hashtable();
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
    private String requestUrl;
    private String serverName;
    private String servletPath;
    private String scheme;
    private int localPort = 0;

    public MockRequest() {
    }

    public MockRequest(String requestURI) {
        this.requestURI = contextPath + requestURI;
        this.requestUrl = requestURI;
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
        return null;
    }

    public void setSessionNew(boolean b) {
    }

    public HttpSession getSession() {
        return null;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
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

    public Enumeration getParameterNames() {
        return null;
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
        return null;
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
        return null;
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

}


class MockResponse implements HttpServletResponse {

    private Hashtable responseHeaders = new Hashtable();
    private int status = 200;
    private String redirectedUrl;
    private List cookies = new ArrayList();
    private Locale locale;

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public boolean containsHeader(String s) {
        return false;
    }

    public String encodeURL(String s) {
        if (s == null) return null;
        if (s.indexOf("http:") == 0) return s;
        if (s.contains("?")) {
            return s.substring(0, s.indexOf("?")) + ";mockencoded=test" + s.substring(s.indexOf("?"), s.length());
        } else {
            return s.concat(";mockencoded=test");
        }
    }

    public String encodeRedirectURL(String s) {
        return encodeURL(s);
    }

    /**
     * @deprecated
     */
    public String encodeUrl(String s) {
        return encodeURL(s);
    }

    /**
     * @deprecated
     */
    public String encodeRedirectUrl(String s) {
        return encodeURL(s);
    }

    public void sendError(int i, String s) throws IOException {

    }

    public void sendError(int i) throws IOException {

    }

    public void sendRedirect(String s) throws IOException {
        redirectedUrl = s;
    }

    public void setDateHeader(String s, long l) {
        responseHeaders.put(s, l + "");
    }

    public void addDateHeader(String s, long l) {
        responseHeaders.put(s, l + "");
    }

    public void setHeader(String s, String s1) {
        responseHeaders.put(s, s1);
    }

    public void addHeader(String s, String s1) {
        responseHeaders.put(s, s1);
    }

    public void setIntHeader(String s, int i) {
        responseHeaders.put(s, i + "");
    }

    public void addIntHeader(String s, int i) {
        responseHeaders.put(s, i + "");
    }

    public void setStatus(int i) {
        status = i;
    }

    /**
     * @deprecated
     */
    public void setStatus(int i, String s) {

    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        return null;
    }

    public void setCharacterEncoding(String s) {

    }

    public void setContentLength(int i) {

    }

    public void setContentType(String s) {

    }

    public void setBufferSize(int i) {

    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void setLocale(Locale l) {
        locale = l;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getHeader(String s) {
        return (String) responseHeaders.get(s);
    }

    public int getStatus() {
        return status;
    }

    public String getRedirectedUrl() {
        return redirectedUrl;
    }

    public List getCookies() {
        return cookies;
    }
}
