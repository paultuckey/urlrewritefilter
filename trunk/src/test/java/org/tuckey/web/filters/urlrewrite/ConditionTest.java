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
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class ConditionTest extends TestCase {

    public void setUp() {
        Log.setLevel("DEBUG");
    }


    public void testValue() {
        Condition condition = new Condition();
        condition.setValue("tEster");
        assertEquals("tEster", condition.getValue());
    }

    public void testName() {
        Condition condition = new Condition();
        condition.setName("mename");
        assertEquals("mename", condition.getName());
    }

    public void testType() {

        typeSpecific("time");
        typeSpecific("year");
        typeSpecific("month");
        typeSpecific("dayofmonth");
        typeSpecific("dayofweek");

        typeSpecific("ampm");
        typeSpecific("hourofday");
        typeSpecific("minute");
        typeSpecific("second");
        typeSpecific("millisecond");

        typeSpecific("attribute");
        typeSpecific("auth-type");
        typeSpecific("character-encoding");
        typeSpecific("content-length");
        typeSpecific("content-type");

        typeSpecific("context-path");
        typeSpecific("cookie");
        typeSpecific("header");
        typeSpecific("method");
        typeSpecific("parameter");

        typeSpecific("path-info");
        typeSpecific("path-translated");
        typeSpecific("protocol");
        typeSpecific("query-string");
        typeSpecific("remote-addr");

        typeSpecific("remote-host");
        typeSpecific("remote-user");
        typeSpecific("requested-session-id");
        typeSpecific("requested-session-id-from-cookie");
        typeSpecific("requested-session-id-from-url");
        typeSpecific("requested-session-id-valid");
        typeSpecific("request-uri");
        typeSpecific("request-url");

        typeSpecific("session-attribute");
        typeSpecific("session-isnew");
        typeSpecific("port");
        typeSpecific("server-name");
        typeSpecific("scheme");

        typeSpecific("user-in-role");
    }

    public void typeSpecific(String type) {
        Condition condition = new Condition();
        condition.setType(type);
        assertEquals(type, condition.getType());

        // perform a weak test to check for null pointers etc
        MockRequest request = new MockRequest();
        condition.initialise();
        condition.getConditionMatch(request);
    }

    public void testCompileFailure() {
        Condition condition = new Condition();
        condition.setValue("aaa[");
        assertFalse("regex shouldn't compile", condition.initialise());
    }

    public void testOperator() {
        Condition condition = new Condition();
        condition.setOperator("");
        assertEquals("equal", condition.getOperator());
    }

    public void testNext() {
        Condition condition = new Condition();
        condition.setNext("and");
        assertEquals("and", condition.getNext());

        Condition condition2 = new Condition();
        condition2.setNext("badand");
        assertFalse("must not init", condition.initialise());

        Condition condition3 = new Condition();
        condition3.setNext("or");
        assertEquals("or", condition3.getNext());
        assertTrue(condition3.isProcessNextOr());
    }

    public void testUnItied() {
        Condition condition = new Condition();
        assertNull(condition.getConditionMatch(new MockRequest()));
    }

    public void testCaseSensitive() {
        Condition condition = new Condition();
        condition.setType("header");
        condition.setName("a");
        condition.setValue("aaa");
        condition.setCaseSensitive(true);
        condition.initialise();
        MockRequest request = new MockRequest();
        request.setHeader("a", "aAa");
        assertNull(condition.getConditionMatch(request));
        request.setHeader("a", "aaa");
        assertNotNull(condition.getConditionMatch(request));
        assertTrue(condition.isCaseSensitive());
    }

    public void testInvalid() {
        Condition condition = new Condition();
        condition.setType("bogus");
        condition.initialise();
        assertNull(condition.getConditionMatch(new MockRequest()));
    }

    public void testInstanceOf() {
        Condition condition = new Condition();
        condition.setType("attribute");
        condition.setOperator("instanceof");
        condition.setName("obj");
        condition.setValue("org.tuckey.web.filters.urlrewrite.ConditionTest");
        condition.initialise();
        MockRequest req = new MockRequest();
        req.setAttribute("obj", this);
        assertNotNull(condition.getConditionMatch(req));
        req.setAttribute("obj", new ConditionTestExtended());
        assertNotNull("subclasses should match", condition.getConditionMatch(req));
    }

    class ConditionTestExtended extends ConditionTest {
        // nothing
    }

    public void testId() {
        Condition condition = new Condition();
        condition.setId(98);
        assertTrue(condition.getId() == 98);
    }

    public void testConditionOperator() {
        MockRequest request = new MockRequest();
        request.setServerPort(10);

        Condition condition = new Condition();
        condition.setType("port");
        condition.setValue("9");
        condition.setOperator("greater");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));

        Condition condition2 = new Condition();
        condition2.setType("port");
        condition2.setValue("11");
        condition2.setOperator("less");
        condition2.initialise();
        assertNotNull("condition must match", condition2.getConditionMatch(request));

        Condition condition3 = new Condition();
        condition3.setType("port");
        condition3.setValue("10");
        condition3.setOperator("greaterorequal");
        condition3.initialise();
        assertNotNull("condition must match", condition3.getConditionMatch(request));

        Condition condition4 = new Condition();
        condition4.setType("port");
        condition4.setValue("10");
        condition4.setOperator("lessorequal");
        condition4.initialise();
        assertNotNull("condition must match", condition4.getConditionMatch(request));

        Condition condition5 = new Condition();
        condition5.setType("port");
        condition5.setValue("99");
        condition5.setOperator("notequal");
        condition5.initialise();
        assertNotNull("condition must match", condition5.getConditionMatch(request));

        Condition condition6 = new Condition();
        condition6.setType("method");
        condition6.setValue("POST");
        condition6.setOperator("notequal");
        condition6.initialise();
        assertNotNull("condition must match", condition6.getConditionMatch(request));

        Condition condition7 = new Condition();
        condition7.setType("method");
        condition7.setValue("POST");
        condition7.setOperator("somebadassop");

        assertFalse("condition must not init", condition7.initialise());
        assertTrue("condition must have error", condition7.getError() != null);

    }

    public void testConditionTime() {
        MockRequest request = new MockRequest();
        long field = System.currentTimeMillis();
        Condition condition = new Condition();
        condition.setType("time");
        condition.setValue("" + field);
        condition.setOperator("greaterorequal");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionYear() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.YEAR);
        Condition condition = new Condition();
        condition.setType("year");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionMonth() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.MONTH);
        Condition condition = new Condition();
        condition.setType("month");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionDayOfMonth() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.DAY_OF_MONTH);
        Condition condition = new Condition();
        condition.setType("dayofmonth");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionDayOfWeek() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.DAY_OF_WEEK);
        Condition condition = new Condition();
        condition.setType("dayofweek");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }


    public void testConditionAmPm() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.AM_PM);
        Condition condition = new Condition();
        condition.setType("ampm");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionHourOfDay() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.HOUR_OF_DAY);
        Condition condition = new Condition();
        condition.setType("hourofday");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionMinute() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.MINUTE);
        Condition condition = new Condition();
        condition.setType("minute");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionSecond() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.SECOND);
        Condition condition = new Condition();
        condition.setType("second");
        condition.setValue("" + field);
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionMillisecond() {
        MockRequest request = new MockRequest();
        Calendar cal = Calendar.getInstance();
        int field = cal.get(Calendar.MILLISECOND);
        Condition condition = new Condition();
        condition.setType("millisecond");
        condition.setValue("" + field);
        condition.setOperator("greaterorequal");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionAttribute() {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("attribute");
        condition.setName("ray");
        condition.setValue("andchristian");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setAttribute("ray", "andchristian");
        assertNotNull("condition must match", condition.getConditionMatch(request));

        Condition condition2 = new Condition();
        condition2.setType("attribute");
        condition2.setName("ray");
        condition2.setValue("andbob");
        condition2.initialise();
        assertNull("condition must not match", condition2.getConditionMatch(request));

        Condition condition3 = new Condition();
        condition3.setType("attribute");
        condition3.setValue("andbob");
        condition3.initialise();
        assertNull("condition must not initialise", condition3.getConditionMatch(request));
    }

    public void testConditionAuthType() {
        MockRequest request = new MockRequest();
        request.setAuthType("pwdwithcrapasrot13");
        Condition condition = new Condition();
        condition.setType("auth-type");
        condition.setValue("pwd[a-z0-9]+");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));

        Condition condition2 = new Condition();
        condition2.setType("auth-type");
        condition2.setValue("someotherpwdtype");
        condition2.initialise();
        assertNull("condition must not match", condition2.getConditionMatch(request));
    }

    public void testConditionCharacterEncoding() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("character-encoding");
        condition.setValue("utfcrazybig[0-9]+");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setCharacterEncoding("utfcrazybig13");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testConditionContentLength() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        request.setContentLength(120);
        Condition condition = new Condition();
        condition.setType("content-length");
        condition.setValue("100");
        condition.setOperator("greater");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));

        request.setContentLength(10);
        assertNull("condition must not match", condition.getConditionMatch(request));
    }

    public void testContentType() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        request.setContentType("bottlesandcans");
        Condition condition = new Condition();
        condition.setType("content-type");
        condition.setValue("bott[a-z]+");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));

        request.setContentType(null);
        assertNull("condition must not match", condition.getConditionMatch(request));
    }

    public void testContextPath() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        request.setContextPath("blah");
        Condition condition = new Condition();
        condition.setType("context-path");
        condition.setValue("[a-b]lah");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));

        request.setContextPath("qlah");
        assertNull("condition must not match", condition.getConditionMatch(request));
    }

    public void testCookie() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("cookie");
        condition.setName("tracker");
        condition.setValue(".*bass.*");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.addCookie(new Cookie("otherokie", "allyourbassisbelongtous"));
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.addCookie(new Cookie("tracker", "allyourbassisbelongtous"));
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testLocalPort() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        request.setLocalPort(4);
        Condition condition = new Condition();
        condition.setType("local-port");
        condition.setValue("1004");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setLocalPort(1004);
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testParameter() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("parameter");
        condition.setName("reqparam");
        condition.setValue("[0-9]+");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.addParameter("reqparam", "1000245");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    // Condition: Equal, Pattern: Null, Value: Null, Result should be: Match.
    public void testParameterNull1() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("parameter");
        condition.setName("reqparam");
        condition.setValue("");
        condition.setOperator("equal");
        condition.initialise();
        assertNull("condition must match", condition.getConditionMatch(request));
    }

    // Condition: Equal, Pattern: Null, Value: Not Null, Result should be: No match.
    public void testParameterNull2() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("parameter");
        condition.setName("reqparam");
        condition.setValue("");
        condition.setOperator("equal");
        condition.initialise();
        request.addParameter("reqparam", "1000245");
        assertNotNull("condition must not match", condition.getConditionMatch(request));
    }

    // Condition: Not Equal, Pattern: Null, Value: Null, Result should be: No match.
    public void testParameterNull3() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("parameter");
        condition.setName("reqparam");
        condition.setValue("");
        condition.setOperator("notequal");
        condition.initialise();
        assertNotNull("condition must not match", condition.getConditionMatch(request));
        request.addParameter("reqparam", "1000245");
    // Condition: Not Equal, Pattern: Null, Value: Not Null, Result should be: match.
        assertNull("condition must match", condition.getConditionMatch(request));
    }

    // Condition: Not Equal, Pattern: Null, Value: Not Null, Result should be: match.
    public void testParameterNull4() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("parameter");
        condition.setName("reqparam");
        condition.setValue("");
        condition.setOperator("notequal");
        condition.initialise();
        request.addParameter("reqparam", "1000245");
        assertNull("condition must match", condition.getConditionMatch(request));
    }

    public void testPathInfo() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("path-info");
        condition.setValue("afr[aeiou]ca");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setPathInfo("africa");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testPathTranslated() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("path-translated");
        condition.setValue("/!@&");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setPathTranslated("/!@&");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testProtocol() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("protocol");
        condition.setValue("HTTP/1\\.[1-2]");
        condition.initialise();
        request.setProtocol("HTTP/2.0");
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setProtocol("HTTP/1.2");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testQueryString() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("query-string");
        condition.setValue(".*&param=[0-9]+.*");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setQueryString("?aaa=dsdsd&param=2333&asdsa=sdds");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRemoteAddr() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("remote-addr");
        condition.setValue("192.168.[0-9]+.[0-9]+");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRemoteAddr("192.168.184.23");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRemoteHost() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("remote-host");
        condition.setValue("\\w+\\.tuckey.org");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRemoteHost("toaster.tuckey.org");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRemoteUser() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("remote-user");
        condition.setValue("p.\\w+");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRemoteUser("p.smith");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRequestedSessionId() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("requested-session-id");
        condition.setValue("\\w+\\.sec[0-6]+");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRequestedSessionId("sdfjsdfhkjhk897fd.sec03");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRequestedSessionIdFromCookie() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("requested-session-id-from-cookie");
        condition.setValue("true");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRequestedSessionIdFromCookie(true);
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRequestedSessionIdFromURL() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("requested-session-id-from-url");
        condition.setValue("true");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRequestedSessionIdFromURL(true);
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRequestedSessionIdValid() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("requested-session-id-valid");
        condition.setValue("true");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRequestedSessionIdValid(true);
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRequestUri() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("request-uri");
        condition.setValue("\\d");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRequestURI("2");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testRequestUrl() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("request-url");
        condition.setValue("\\d");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setRequestURL("2");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testSessionAttribute() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("session-attribute");
        condition.setValue("someval");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        Condition condition2 = new Condition();
        condition2.setType("session-attribute");
        condition2.setName("someatt");
        condition2.setValue("someval");
        condition2.initialise();
        assertNull("condition must not match", condition2.getConditionMatch(request));

        request.getSession(true).setAttribute("someatt", "someval");

        Condition condition3 = new Condition();
        condition3.setType("session-attribute");
        condition3.setName("someatt");
        condition3.setValue("someval");
        condition3.initialise();
        assertNotNull("condition must match", condition3.getConditionMatch(request));
    }

    public void testSessionIsNew() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("session-isnew");
        condition.setValue("yes");
        condition.setOperator("notequal");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));

        request.setSessionNew(true);
        assertNull("condition must not match", condition.getConditionMatch(request));
    }

    public void testServerName() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("server-name");
        condition.setValue("dev.*");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setServerName("dev.googil.com");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }

    public void testScheme() throws UnsupportedEncodingException {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setType("scheme");
        condition.setValue("http");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setScheme("http");
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }


    public void testConditionHeader() {
        MockRequest request = new MockRequest();
        Condition condition = new Condition();
        condition.setName("some header");
        condition.setValue("tester");
        condition.initialise();
        assertNull("condition must not match", condition.getConditionMatch(request));

        request.setHeader("some header", "tester");
        assertNotNull("condition must match", condition.getConditionMatch(request));

        Condition condition2 = new Condition();
        condition2.setName("  ");
        condition2.setValue("tester");
        assertFalse("condition must not initialise", condition2.initialise());
        assertNull("condition must not match", condition2.getConditionMatch(request));

        Condition condition3 = new Condition();
        condition3.setName("bonus");
        assertTrue("condition must initialise and check for exists", condition3.initialise());
        assertNull("condition must not match", condition3.getConditionMatch(request));
        request.setHeader("bonus", "tester");
        assertNotNull("condition must match", condition3.getConditionMatch(request));

        Condition condition4 = new Condition();
        condition4.setName("portashed");
        condition4.setOperator("notequal");

        assertTrue("condition must initialise and check for exists", condition4.initialise());
        assertNotNull("condition must match", condition4.getConditionMatch(request));
        request.setHeader("portashed", "tester");
        assertNull("condition must not match", condition4.getConditionMatch(request));

    }

    public void testConditionMethod() {
        MockRequest request = new MockRequest();
        request.setMethod("HEAD");
        Condition condition = new Condition();
        condition.setType("method");
        condition.setValue("H[A-Z]AD");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));
    }


    public void testConditionIsUserInRole() {
        MockRequest request = new MockRequest();
        request.addRole("devil");
        Condition condition = new Condition();
        condition.setType("user-in-role");
        condition.setName("devil");
        condition.initialise();
        assertNotNull("user should be in this role", condition.getConditionMatch(request));

        Condition condition2 = new Condition();
        condition2.setType("user-in-role");
        condition2.setName("angel");
        condition2.initialise();
        assertNull("bad user in role must not match", condition2.getConditionMatch(request));

        Condition condition3 = new Condition();
        condition3.setType("user-in-role");
        condition3.setValue("devil");
        condition3.initialise();
        assertNotNull("value instead of name should match", condition3.getConditionMatch(request));

        Condition condition4 = new Condition();
        condition4.setType("user-in-role");
        condition4.setValue("admin");
        condition4.setOperator("notequal");
        condition4.initialise();
        assertNotNull("value instead of name should match", condition4.getConditionMatch(request));
    }

    public void testConditionPort() {
        MockRequest request = new MockRequest();
        request.setServerPort(9001);
        Condition condition = new Condition();
        condition.setType("port");
        condition.setValue("9001");
        condition.initialise();
        assertNotNull("condition must match", condition.getConditionMatch(request));

        Condition condition2 = new Condition();
        condition2.setType("port");
        condition2.setValue(" 9001");
        condition2.initialise();
        assertNotNull("condition must match", condition2.getConditionMatch(request));
        // check re-init
        condition2.initialise();
        assertNotNull("condition must match", condition2.getConditionMatch(request));

        Condition condition3 = new Condition();
        condition3.setType("port");
        condition3.setValue("aaa");
        condition3.initialise();
        assertNull("condition must not match", condition3.getConditionMatch(request));
    }


}
