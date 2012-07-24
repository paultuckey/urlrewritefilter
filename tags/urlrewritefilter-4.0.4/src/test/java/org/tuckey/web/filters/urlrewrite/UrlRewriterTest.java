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
import org.tuckey.web.filters.urlrewrite.test.MockRewriteMatch;
import org.tuckey.web.filters.urlrewrite.test.TestRunObj;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.testhelper.MockFilterChain;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;
import org.tuckey.web.testhelper.MockServletContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Paul Tuckey
 * @version $Revision: 44 $ $Date: 2006-11-02 12:29:14 +1300 (Thu, 02 Nov 2006) $
 */
public class UrlRewriterTest extends TestCase {

    MockResponse response;
    MockRequest request;
    MockServletContext servletContext;
    MockFilterChain chain;

    public void setUp() {
        Log.setLevel("DEBUG");
        response = new MockResponse();
        request = new MockRequest("/");
        servletContext = new MockServletContext();
        chain = new MockFilterChain();
        TestRunObj.resetTestFlags();
    }

    public void test1() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule1 = new NormalRule();
        rule1.setFrom("/aaa");
        rule1.setTo("/bbb");
        conf.addRule(rule1);
        conf.setUseQueryString(true);
        conf.setDecodeUsing("null");
        conf.initialise();

        assertFalse(conf.isDecodeUsingCustomCharsetRequired());

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/aaa");
        NormalRewrittenUrl rewrittenRequest = (NormalRewrittenUrl) urlRewriter.processRequest(request, response);

        assertTrue(rewrittenRequest.isForward());
        assertEquals("/bbb", rewrittenRequest.getTarget());

        // test null url
        request = new MockRequest();
        rewrittenRequest = (NormalRewrittenUrl) urlRewriter.processRequest(request, response);
        assertNull(rewrittenRequest);

        // test query string
        request = new MockRequest("/aaa");
        request.setQueryString("bb=cc");
        rewrittenRequest = (NormalRewrittenUrl) urlRewriter.processRequest(request, response);
        assertEquals("/bbb?bb=cc", rewrittenRequest.getTarget());

        request = new MockRequest("/aaa%20");
        rewrittenRequest = (NormalRewrittenUrl) urlRewriter.processRequest(request, response);
        assertEquals("/bbb%20", rewrittenRequest.getTarget());
    }

    public void testDefaultDecode() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule1 = new NormalRule();
        rule1.setFrom("^/ca&amp;t/(.*)$");
        rule1.setTo("/search/?c=y&amp;q=$1");
        conf.addRule(rule1);
        conf.initialise();

        assertTrue("isDecodeUsingEncodingHeader should be true", conf.isDecodeUsingEncodingHeader());
        assertTrue("isDecodeUsingCustomCharsetRequired should be true", conf.isDecodeUsingCustomCharsetRequired());
        UrlRewriter urlRewriter = new UrlRewriter(conf);
        MockRequest request = new MockRequest("/ca&amp;t/abc");
        NormalRewrittenUrl rewrittenRequest = (NormalRewrittenUrl) urlRewriter.processRequest(request, response);

        assertTrue("should be forward", rewrittenRequest.isForward());
        assertEquals("/search/?c=y&amp;q=abc", rewrittenRequest.getTarget());
    }

    public void testAbsoluteRedir() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule1 = new NormalRule();
        rule1.setFrom("^(.*)$");
        rule1.setToType("permanent-redirect");
        rule1.setTo("http://sss.sss.sss");
        conf.addRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/aaa");
        RewrittenUrl rewrittenRequest = urlRewriter.processRequest(request, response);

        rewrittenRequest.doRewrite(request, response, null);
        assertEquals("http://sss.sss.sss", response.getHeader("Location"));

    }


    public void testNoRules() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);
        MockRequest request = new MockRequest("/aaa");
        RewrittenUrl rewrittenRequest = urlRewriter.processRequest(request, response);
        assertNull(rewrittenRequest);
    }


    public void testSetSimple() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule1 = new NormalRule();
        rule1.setFrom("^/aaa([0-9]+)(.*)$");
        SetAttribute setAttribute1 = new SetAttribute();
        setAttribute1.setName("blah");
        setAttribute1.setValue("someval");
        rule1.addSetAttribute(setAttribute1);

        SetAttribute setAttribute2 = new SetAttribute();
        setAttribute2.setType("session");
        setAttribute2.setName("sblah");
        setAttribute2.setValue("$1");
        rule1.addSetAttribute(setAttribute2);

        SetAttribute setAttribute3 = new SetAttribute();
        setAttribute3.setType("response-header");
        setAttribute3.setName("Cache-control");
        setAttribute3.setValue("none");
        rule1.addSetAttribute(setAttribute3);

        SetAttribute setAttribute4 = new SetAttribute();
        setAttribute4.setName("action");
        setAttribute4.setValue("delete");
        rule1.addSetAttribute(setAttribute4);

        conf.addRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/aaa4?asdadasd");
        urlRewriter.processRequest(request, response);

        assertEquals("someval", request.getAttribute("blah"));

        assertEquals("4", request.getSession().getAttribute("sblah"));
        assertEquals("delete", request.getAttribute("action"));
        assertEquals("none", response.getHeader("Cache-control"));

    }

    public void testSetSimpleWithRule() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule0 = new NormalRule();
        rule0.setFrom("/bbb([0-9]+)");
        rule0.setTo("/qqq");
        rule0.setToLast("false");

        NormalRule rule1 = new NormalRule();
        rule1.setFrom("^/aaa([0-9]+)$");
        SetAttribute setAttribute1 = new SetAttribute();
        setAttribute1.setType("status");
        setAttribute1.setValue("404");
        rule1.addSetAttribute(setAttribute1);

        conf.addRule(rule0);
        conf.addRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/bbb2?asdadasd");
        RewrittenUrl rewrittenRequest = urlRewriter.processRequest(request, response);

        assertEquals("/qqq?asdadasd", rewrittenRequest.getTarget());
        assertEquals(200, response.getStatus());

    }

    public void testBadConf() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule1 = new NormalRule();
        conf.addRule(rule1);
        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);
        MockRequest request = new MockRequest("/aaa");
        RewrittenUrl rewrittenRequest = urlRewriter.processRequest(request, response);
        assertNull(rewrittenRequest);
    }

    /**
     * Special test for rule application with no to element.
     */
    public void testNoToElement() throws IOException, ServletException, InvocationTargetException {
        NormalRule rule1 = new NormalRule();
        rule1.setFrom(".*");
        Condition condition1 = new Condition();
        condition1.setType("header");
        condition1.setName("accept");
        condition1.setValue(".*image/gif.*");
        SetAttribute setAttribute1 = new SetAttribute();
        setAttribute1.setName("imageType");
        setAttribute1.setValue("gif");
        rule1.addSetAttribute(setAttribute1);
        rule1.addCondition(condition1);
        rule1.initialise(null);

        NormalRule rule2 = new NormalRule();
        rule2.setFrom(".*");
        Condition condition2 = new Condition();
        condition2.setType("header");
        condition2.setName("accept");
        condition2.setValue(".*image/jpeg.*");
        SetAttribute setAttribute2 = new SetAttribute();
        setAttribute2.setName("imageType");
        setAttribute2.setValue("jpeg");
        rule2.addSetAttribute(setAttribute2);
        rule2.addCondition(condition2);
        rule2.initialise(null);

        NormalRule rule3 = new NormalRule();
        rule3.setFrom(".*");
        Condition condition3 = new Condition();
        condition3.setType("header");
        condition3.setName("accept");
        condition3.setValue(".*image/png.*");
        SetAttribute setAttribute3 = new SetAttribute();
        setAttribute3.setName("imageType");
        setAttribute3.setValue("png");
        rule3.addSetAttribute(setAttribute3);
        rule3.addCondition(condition3);
        rule3.initialise(null);

        NormalRule rule4 = new NormalRule();
        rule4.setFrom(".*");
        Condition condition4 = new Condition();
        condition4.setType("header");
        condition4.setName("user-agent");
        condition4.setValue("SonyEricssonT68.*");
        SetAttribute setAttribute4 = new SetAttribute();
        setAttribute4.setName("imageType");
        setAttribute4.setValue("png");
        rule4.addSetAttribute(setAttribute4);
        rule4.addCondition(condition4);
        rule4.initialise(null);

        Conf conf = new Conf();
        conf.addRule(rule1);
        conf.addRule(rule2);
        conf.addRule(rule3);
        conf.addRule(rule4);

        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/images/my.png");
        request.setHeader("accept", "image/png,*/*;q=0.5");

        RewrittenUrl rewrittenUrl1 = urlRewriter.processRequest(request, response);

        assertEquals("png", (String) request.getAttribute("imageType"));
        assertEquals(null, rewrittenUrl1);

    }


    public void testRuleDecode() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule = new NormalRule();
        rule.setFrom("^/test decode/(.+?)$");
        rule.setTo("/TestHandler$1");
        conf.addRule(rule);
        conf.setDecodeUsing("utf-8");
        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);
        MockRequest request = new MockRequest("/test%20decode/?string=black%26white+green");
        MockResponse response = new MockResponse();
        RewrittenUrl rewrittenUrl = urlRewriter.processRequest(request, response);
        assertEquals("forward should be default type", "forward", rule.getToType());
        assertEquals("/TestHandler?string=black&white green", rewrittenUrl.getTarget());
    }


    public void testRuleChain() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf(servletContext, null, null, null);

        Run run = new Run();
        run.setClassStr(TestRunObj.class.getName());
        run.setMethodStr("runWithChainParam( req, res, chain )");

        NormalRule rule = new NormalRule();
        rule.setFrom("^/$");
        rule.addRun(run);
        conf.addRule(rule);

        Run run2 = new Run();
        run2.setClassStr(TestRunObj.class.getName());
        run2.setMethodStr("runWithReturnedObj");

        NormalRule rule2 = new NormalRule();
        rule2.setFrom("^/$");
        rule2.setTo("null");
        rule2.addRun(run2);
        rule2.setToLast("true");
        conf.addRule(rule2);

        conf.initialise();

        assertTrue("conf should be initialised", conf.isOk());

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        run.execute(request, response, null, chain);
        urlRewriter.processRequest(request, response);

        assertEquals("chain chould have been called once", 1, chain.getInvocationCount());
        assertEquals("forward should be default type", "forward", rule.getToType());
        long diff = TestRunObj.getRunWithChainParamAfterDoFilter() - MockRewriteMatch.getCalledTime();
        assertTrue("run2 should be invoked after chain " + diff, diff > 0);
    }


    public void testNoSubstitutionLast() throws IOException, ServletException, InvocationTargetException {
        final NormalRule rule1 = new NormalRule();
        rule1.setFrom("noSub");
        rule1.setTo("-");
        rule1.setToLast("true");
        rule1.initialise(null);

        final NormalRule rule2 = new NormalRule();
        rule2.setFrom("noS");
        rule2.setTo("changed");
        rule2.initialise(null);

        final Conf conf = new Conf();
        conf.addRule(rule1);
        conf.addRule(rule2);

        conf.initialise();
        final UrlRewriter urlRewriter = new UrlRewriter(conf);

        final MockRequest request1 = new MockRequest("/path/noSub");
        final RewrittenUrl rewrittenUrl1 = urlRewriter.processRequest(request1, response);
        assertNotNull(rewrittenUrl1);
        assertEquals("/path/noSub", rewrittenUrl1.getTarget());

        final MockRequest request2 = new MockRequest("/path/noSu");
        final RewrittenUrl rewrittenUrl2 = urlRewriter.processRequest(request2, response);
        assertNotNull(rewrittenUrl2);
        assertEquals("/path/changedu", rewrittenUrl2.getTarget());
    }

    public void testQueryToPath() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        conf.setDecodeUsing("null");
        NormalRule rule1 = new NormalRule();
        rule1.setFrom("^/\\?q=(.*)$");
        rule1.setTo("/search/${escapePath:${unescape:$1}}");
        conf.addRule(rule1);
        conf.initialise();

        assertFalse("isDecodeUsingEncodingHeader should be false", conf.isDecodeUsingEncodingHeader());
        assertFalse("isDecodeUsingCustomCharsetRequired should be false", conf.isDecodeUsingCustomCharsetRequired());
        UrlRewriter urlRewriter = new UrlRewriter(conf);
        MockRequest request = new MockRequest("/?q=foo+bar%2bgee");
        NormalRewrittenUrl rewrittenRequest = (NormalRewrittenUrl) urlRewriter.processRequest(request, response);

        assertTrue("should be forward", rewrittenRequest.isForward());
        assertEquals("/search/foo%20bar+gee", rewrittenRequest.getTarget());
    }

    public void testPathToQuery() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        conf.setDecodeUsing("null");
        NormalRule rule1 = new NormalRule();
        rule1.setFrom("^/(.*)$");
        rule1.setTo("/?q=${escape:${unescapePath:$1}}");
        conf.addRule(rule1);
        conf.initialise();

        assertFalse("isDecodeUsingEncodingHeader should be false", conf.isDecodeUsingEncodingHeader());
        assertFalse("isDecodeUsingCustomCharsetRequired should be false", conf.isDecodeUsingCustomCharsetRequired());
        UrlRewriter urlRewriter = new UrlRewriter(conf);
        MockRequest request = new MockRequest("/foo+bar%20gee");
        NormalRewrittenUrl rewrittenRequest = (NormalRewrittenUrl) urlRewriter.processRequest(request, response);

        assertTrue("should be forward", rewrittenRequest.isForward());
        assertEquals("/?q=foo%2Bbar+gee", rewrittenRequest.getTarget());
    }

}

