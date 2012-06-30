package org.tuckey.web.filters.urlrewrite.test;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.http.HttpServletRequest;

public class MockRequestParserTest extends TestCase {

    private MockRequestParser parser = new MockRequestParser();

    public void setUp() {
        Log.setLevel("DEBUG");
    }


    public void testSimpleOneLine() {
        HttpServletRequest request = parser.decodeRequest("/test/uri");
        assertEquals("/test/uri", request.getRequestURI());
    }

    public void testOneLineSessionId() {
        HttpServletRequest request = parser.decodeRequest("/test/uri;jsessionid=asdf");
        assertEquals("/test/uri", request.getRequestURI());
        assertEquals("asdf", request.getRequestedSessionId());
    }

    public void testOneLineSessionIdAndQueryString() {
        HttpServletRequest request = parser.decodeRequest("/test/uri;jsessionid=asdf?abc=123");
        assertEquals("/test/uri", request.getRequestURI());
        assertEquals("asdf", request.getRequestedSessionId());
        assertEquals("123", request.getParameter("abc"));
    }

    public void testSimpleOneLine2() {
        HttpServletRequest request = parser.decodeRequest("/test/uri?abc=123&asd=345");
        assertEquals("/test/uri", request.getRequestURI());
        assertEquals("abc=123&asd=345", request.getQueryString());
        assertEquals("123", request.getParameter("abc"));
        assertEquals("345", request.getParameter("asd"));
    }

    public void testSimpleTwoLine() {
        HttpServletRequest request = parser.decodeRequest("POST /pub/WWW/TheProject.html HTTP/1.1\n" +
                "Host: www.w3.org\n" +
                "user-agent: Mozburger");
        assertEquals("/pub/WWW/TheProject.html", request.getRequestURI());
        assertEquals("POST", request.getMethod());
        assertEquals("HTTP/1.1", request.getScheme());
        assertEquals("www.w3.org", request.getServerName());
        assertEquals("Mozburger", request.getHeader("user-agent"));
    }

    public void testPost() {
        HttpServletRequest request = parser.decodeRequest("POST /pub/WWW/TheProject.html HTTP/1.1\n" +
                "Host: www.w3.org\n" +
                "\n" +
                "id=23&fast&name=bert");
        assertEquals("/pub/WWW/TheProject.html", request.getRequestURI());
        assertEquals("POST", request.getMethod());
        assertEquals("HTTP/1.1", request.getScheme());
        assertEquals("www.w3.org", request.getServerName());
        assertEquals("23", request.getParameter("id"));
        assertEquals("bert", request.getParameter("name"));
    }


}
