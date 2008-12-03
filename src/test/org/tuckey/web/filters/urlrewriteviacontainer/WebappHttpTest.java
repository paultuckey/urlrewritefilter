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
package org.tuckey.web.filters.urlrewriteviacontainer;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.tuckey.web.filters.urlrewrite.test.TestRunObj;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import java.io.IOException;


/**
 * @author Paul Tuckey
 * @version $Revision: 33 $ $Date: 2006-09-12 16:41:56 +1200 (Tue, 12 Sep 2006) $
 */
public class WebappHttpTest extends ContainerTestBase {

    private String CONTENT = "<p>some content</p>";

    protected String getApp() {
        return "webapp";
    }

    public void testStatusRecord() throws IOException {
        super.recordRewriteStatus();
    }

    public void testProduct() throws IOException, SAXException, InterruptedException {
        GetMethod method = new GetMethod(getBaseUrl() + "/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }


    public void testSimpleDistEx() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/test/status/");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/rewrite-status", method.getResponseHeader("Location").getValue());
    }

    public void testBasicSets() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/settest/674");
        client.executeMethod(method);
        assertNotNull(method.getResponseHeader("cache-control"));
        assertEquals("testsession: hello!, " +
                "param.settest1: 674, " +
                "method: DELETE", method.getResponseBodyAsString());
    }

    public void testMultipleProduct() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/multiple/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }

    public void testNullTo() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/hideme/anb.jsp;dsaddd?asdasds#sdsfd");
        client.executeMethod(method);
        assertEquals("should have status set", 403, method.getStatusCode());
        assertFalse("should not output above content", CONTENT.equals(StringUtils.trim(method.getResponseBodyAsString())));
    }

    public void testYear() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/time/year/current");
        client.executeMethod(method);
        assertEquals("echo yearisbetween1970and3000", method.getResponseBodyAsString());
    }

    public void testTestAxis() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/services/blah?qwerty");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/axis/services/blah", method.getResponseHeader("Location").getValue());
    }

    public void testTestErik() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/eriktest/hi.ho");
        method.setFollowRedirects(false);
        method.addRequestHeader(new Header("host", "blah.com"));
        client.executeMethod(method);
        assertEquals("http://www.example.com/context/hi.ho", method.getResponseHeader("Location").getValue());
    }

    public void testTestEncode() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/went%20to%20bahamas/;jsessionid=12243");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/bahamas/;jsess", method.getResponseHeader("Location").getValue());
    }

    public void testSimpleRun() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/run/test/test1");
        client.executeMethod(method);
        assertEquals("this is " + TestRunObj.class.getName(), method.getResponseBodyAsString());
    }

    public void testQueryStringEscape() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/query-string-escape/jack+%26+jones");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("http://query-string-escape-result.com/?q=jack+%26+jones&another=jack & jones", method.getResponseHeader("Location").getValue());
    }
}
