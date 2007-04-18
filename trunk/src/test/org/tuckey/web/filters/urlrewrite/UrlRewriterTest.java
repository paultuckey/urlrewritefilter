/**
 * Copyright (c) 2005, Paul Tuckey
 * All rights reserved.
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;
import org.tuckey.web.testhelper.BenchmarkRunner;
import org.tuckey.web.testhelper.MockFilterChain;
import org.tuckey.web.testhelper.MockServletContext;
import org.tuckey.web.testhelper.MockRewriteMatch;
import org.tuckey.web.filters.urlrewrite.utils.Log;

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

        assertFalse(conf.isDecodeRequired());

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
     * Goal is to be able to process 2000 reqs a second for a simple rule set of 10000.
     * Performance is obviously very CPU dependant, so we establish a benchmark for the machine the
     * test is running on then base performance on that.
     */
    public void testLoadsOfRules() throws IOException, ServletException, InvocationTargetException {
        // turn off logging
        Log.setLevel("ERROR");

        // benchmark this machine to see what it can do...
        float bench = BenchmarkRunner.establishBenchmark();
        float timePerRule = bench * (float) 0.0003; // ms per rule... 0.03% of the benchmark
        System.out.print("using " + timePerRule + "ms per rule as the standard");

        float testAmount = 10000; // number of times to run test

        // test with 1000 rules, more than anybody would normally have
        Conf conf = new Conf();
        for (int i = 0; i < 1000; i++) {
            NormalRule rule = new NormalRule();
            rule.setFrom("^/([a-z]+)/([0-9]+)/" + i + "/$");
            rule.setTo("/blah/a/$2/");
            conf.addRule(rule);
        }
        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/dir/999/45/");
        // warm up
        urlRewriter.processRequest(request, response);

        long timeStart = System.currentTimeMillis();
        for (float i = 0; i < testAmount; i++) {
            urlRewriter.processRequest(request, response);
            if (i % 500 == 0 && i > 0) {
                System.out.println("avg so far " + ((System.currentTimeMillis() - timeStart) / i) + "ms per rule");
            }
        }
        long took = System.currentTimeMillis() - timeStart;
        System.out.println("took " + took + "ms " + (took / testAmount) + "ms  per rule");
        assertTrue("should take less than " + timePerRule + "ms per rule", (took / testAmount) < timePerRule);
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
        rule.setFrom("^/test-decode/(.+?)$");
        rule.setTo("/TestHandler$1");
        conf.addRule(rule);
        conf.setDecodeUsing("null");

        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/test-decode/?string=black%26white");
        MockResponse response = new MockResponse();
        //request.setQueryString("black%26white");
        RewrittenUrl rewrittenUrl = urlRewriter.processRequest(request, response);

        assertEquals("forward should be default type", "forward", rule.getToType());
        assertEquals("/TestHandler?string=black%26white", rewrittenUrl.getTarget());
    }


    public void testRuleChain() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf(servletContext, null, null, null);

        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("runWithChainParam( req, res, chain )");

        NormalRule rule = new NormalRule();
        rule.setFrom("^/$");
        rule.addRun(run);
        conf.addRule(rule);

        Run run2 = new Run();
        run2.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
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

        run.execute(request, response, null, chain );
        urlRewriter.processRequest(request, response);

        assertEquals("chain chould have been called once", 1, chain.getInvocationCount());
        assertEquals("forward should be default type", "forward", rule.getToType());
        long diff = TestRunObj.getRunWithChainParamAfterDoFilter() - MockRewriteMatch.getCalledTime();
        assertTrue("run2 should be invoked after chain " + diff, diff > 0 );
    }

}

