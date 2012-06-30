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
package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.NumberUtils;
import org.tuckey.web.filters.urlrewrite.utils.RegexMatcher;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;

import javax.servlet.http.Cookie;
import java.util.regex.Pattern;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class SetAttributeTest extends TestCase {

    private RegexMatcher toMatcher;

    public void setUp() {
        Log.setLevel("DEBUG");
        // Create a matcher to pass to execute
        toMatcher = new RegexMatcher(Pattern.compile("^(.*)$").matcher("/query"));
        toMatcher.find();
    }


    public void testBasic() {
        SetAttribute set = new SetAttribute();
        set.setName("blah");
        set.setValue("mercuryrev");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();

        set.initialise();
        set.execute(null, toMatcher, request, response);
        assertTrue(request.getAttribute("blah").equals("mercuryrev"));

    }

    public void testCaptureGroup() {
        SetAttribute set = new SetAttribute();
        set.setName("blah");
        set.setValue("Capture group 1 is $1");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();

        set.initialise();
        set.execute(null, toMatcher, request, response);
        assertTrue(request.getAttribute("blah").equals("Capture group 1 is /query"));
    }

    public void testVariable() {
        SetAttribute set = new SetAttribute();
        set.setType("cookie");
        set.setName("v");
        set.setValue("%{parameter:v}");
        MockRequest request = new MockRequest();
        request.setParameter("v", "1234");
        MockResponse response = new MockResponse();

        set.initialise();
        set.execute(null, toMatcher, request, response);
        assertEquals("v", ((Cookie) response.getCookies().get(0)).getName());
        assertEquals("1234", ((Cookie) response.getCookies().get(0)).getValue());

    }

    public void testVariableWithDollar() {
        SetAttribute set = new SetAttribute();
        set.setName("v");
        set.setValue("%{parameter:v}");
        MockRequest request = new MockRequest();
        request.setParameter("v", "$2");
        MockResponse response = new MockResponse();

        set.initialise();
        set.execute(null, toMatcher, request, response);
        assertEquals("$2", request.getAttribute("v"));

    }

    public void testExpires() throws InterruptedException {
        SetAttribute set = new SetAttribute();

        assertEquals(1000 * 60 * 60 * 24, set.parseTimeValue("1 days"));
        assertEquals(1000 * 60 * 60 * 24, set.parseTimeValue("1 day"));
        assertEquals(1000 * 60 * 60 * 24, set.parseTimeValue(" 1    day "));
        assertEquals((1000 * 60 * 60 * 24) + 4000, set.parseTimeValue("1 day 4 seconds"));
        assertEquals(Math.round(1000 * 60 * 60 * 24 * 365.25), set.parseTimeValue("1 year"));

        set.setType("expires");
        set.setValue("access plus 1 day   2  hours");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        set.initialise();

        long oneDayPlusTwoHours = (1000 * 60 * 60 * 24) + (2 * 1000 * 60 * 60);
        long before = System.currentTimeMillis() + oneDayPlusTwoHours;
        Thread.sleep(100);

        set.execute(null, toMatcher, request, response);

        Thread.sleep(100);
        long after = System.currentTimeMillis() + oneDayPlusTwoHours;
        long expiresValue = NumberUtils.stringToLong(response.getHeader("Expires"));
        assertTrue(expiresValue + " needs to be greater than " + before, expiresValue > before);
        assertTrue(expiresValue + " needs to be less than " + after, expiresValue < after);
    }

    public void testTypeStatus() {
        SetAttribute set = new SetAttribute();
        set.setType("status");
        set.setValue("999");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        set.initialise();
        set.execute(null, toMatcher, request, response);
        assertEquals(999, response.getStatus());
    }

    public void testTypeEmptyStr() {
        SetAttribute set = new SetAttribute();
        set.setType("parameter");
        set.setName("version_id");
        set.setValue(null);
        MockRequest request = new MockRequest();
        set.initialise();
        UrlRewriteWrappedResponse wrappedResponse = new UrlRewriteWrappedResponse(new MockResponse(), request, null);
        set.execute(null, toMatcher, request, wrappedResponse);

        UrlRewriteWrappedRequest wrappedRequest = new UrlRewriteWrappedRequest(request, wrappedResponse.getOverridenRequestParameters(), null);
        assertEquals(null, wrappedRequest.getParameter("version_id"));
    }

    public void testTypeCookie() {
        SetAttribute set = new SetAttribute();
        set.setType("cookie");
        set.setName("mycook");
        set.setValue("someval dasd:blah.com:89009:/ass");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        set.initialise();
        set.execute(null, toMatcher, request, response);
        Cookie cookie = (Cookie) response.getCookies().get(0);
        assertEquals("blah.com", cookie.getDomain());
        assertEquals(89009, cookie.getMaxAge());
        assertEquals("someval dasd", cookie.getValue());
        assertEquals("/ass", cookie.getPath());
    }

    public void testTypeLocale() {
        SetAttribute set = new SetAttribute();
        set.setType("locale");
        set.setValue("en");
        assertTrue(set.initialise());

        set.setValue("en-NZ");
        assertTrue(set.initialise());

        set.setValue("en-NZ-slang");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        set.initialise();
        set.execute(null, toMatcher, request, response);
        assertEquals("slang", response.getLocale().getVariant());
    }

    public void testSetParam() {
        SetAttribute set = new SetAttribute();
        set.setName("blah");
        set.setType("parameter");
        set.setValue("Capture group 1 is $1");
        MockRequest request = new MockRequest();
        UrlRewriteWrappedResponse response = new UrlRewriteWrappedResponse(new MockResponse(), request, null);

        set.initialise();
        set.execute(null, toMatcher, request, response);

        UrlRewriteWrappedRequest wrappedRequest = new UrlRewriteWrappedRequest(request, response.getOverridenRequestParameters(), null);
        assertEquals("Capture group 1 is /query", wrappedRequest.getParameter("blah"));
        assertEquals("Capture group 1 is /query", wrappedRequest.getParameterValues("blah")[0]);

    }

    public void testSetMethod() {
        SetAttribute set = new SetAttribute();
        set.setType("method");
        set.setValue("PUT");
        MockRequest request = new MockRequest();
        UrlRewriteWrappedResponse response = new UrlRewriteWrappedResponse(new MockResponse(), request, null);

        set.initialise();
        set.execute(null, toMatcher, request, response);
        assertTrue(response.getOverridenMethod().equals("PUT"));
    }

}
