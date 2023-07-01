/**
 * Copyright (c) 2005-2023, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 * <p>
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * @author Paul Tuckey
 * @version $Revision: 33 $ $Date: 2006-09-12 16:41:56 +1200 (Tue, 12 Sep 2006) $
 */
public class WebappHttpITTest extends ContainerTestBase {

    protected String getApp() {
        return "webapp";
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        super.setUp();
        super.recordRewriteStatus();
    }

    @AfterEach
    public void afterEach() throws InterruptedException {
        super.tearDown();
    }

    @Test
    public void testProduct() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }


    @Test
    public void testSimpleDistEx() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/test/status/");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("/" + getApp() + "/rewrite-status", method.getResponseHeader("Location").getValue());
    }

    @Test
    public void testBasicSets() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/settest/674");
        client.executeMethod(method);
        assertNotNull(method.getResponseHeader("cache-control"));
        assertEquals("testsession: hello!, " +
                "param.settest1: 674, " +
                "method: GET", method.getResponseBodyAsString());
    }

    @Test
    public void testMultipleProduct() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/multiple/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }

    @Test
    public void testNullTo() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/hideme/anb.jsp;dsaddd?asdasds#sdsfd");
        client.executeMethod(method);
        assertEquals(403, method.getStatusCode()); // "should have status set",
        String CONTENT = "<p>some content</p>";
        assertFalse(CONTENT.equals(StringUtils.trim(method.getResponseBodyAsString()))); // "should not output above content"
    }

    @Test
    public void testYear() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/time/year/current");
        client.executeMethod(method);
        assertEquals("echo yearisbetween1970and3000", method.getResponseBodyAsString());
    }

    @Test
    public void testTestAxis() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/services/blah?qwerty");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("/" + getApp() + "/axis/services/blah", method.getResponseHeader("Location").getValue());
    }

    @Test
    public void testTestErik() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/eriktest/hi.ho");
        method.setFollowRedirects(false);
        method.addRequestHeader(new Header("host", "blah.com"));
        client.executeMethod(method);
        assertEquals("http://www.example.com/context/hi.ho", method.getResponseHeader("Location").getValue());
    }

    @Test
    public void testTestEncode() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/went%20to%20bahamas/");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("/" + getApp() + "/jamaica/", method.getResponseHeader("Location").getValue());
    }

    @Test
    public void testSimpleRun() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/run/test/test1");
        client.executeMethod(method);
        assertEquals("this is " + TestRunObj.class.getName(), method.getResponseBodyAsString());
    }

    @Test
    public void testQueryStringEscape() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/query-string-escape/jack+%26+jones");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("http://query-string-escape-result.com/?q=jack%2B%26%2Bjones&another=jack+&+jones", method.getResponseHeader("Location").getValue());
    }

    @Test
    public void testGzip() throws IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/gzip-me.jsp");
        method.addRequestHeader("accept-encoding", "gzip");
        client.executeMethod(method);
        assertEquals("gzip", method.getResponseHeader("Content-Encoding").getValue());
        assertEquals("hello world hello world hello world", inflateGzipToString(method.getResponseBodyAsStream()));
    }

    /**
     * inflate a gzipped inputstream and return it as a string.
     */
    private String inflateGzipToString(InputStream is) throws IOException {
        GZIPInputStream gis = new GZIPInputStream(is);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            int bytesRead = gis.read(buffer);
            if (bytesRead == -1) break;
            os.write(buffer, 0, bytesRead);
        }
        return os.toString();
    }

}
