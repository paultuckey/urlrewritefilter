package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.test.TestRunObj;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.testhelper.*;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * A quick way to check the performance of the engine.  Should not be a repleacement for proper performance testing!
 */
public class PerformanceTest extends TestCase {

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


    /**
     * Goal is to be able to process 2000 reqs a second for a simple rule set of 10000.
     * Performance is obviously very CPU dependant, so we establish a benchmark for the machine the
     * test is running on then base performance on that. This is a horribly rough way of determining
     * performance, but it is good enough for this simple test case.
     */
    public void testLoadsOfRules() throws IOException, ServletException, InvocationTargetException {
        // turn off logging
        Log.setLevel("ERROR");

        // benchmark this machine to see what it can do...
        float bench = new BenchmarkRunner().establishBenchmark();
        float timePerRule = bench * (float) 0.0003; // ms per rule... 0.03% of the benchmark
        System.out.println("using " + timePerRule + "ms per rule as the standard");

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
     * Goal is to be able to process 1000 urls a second for a simple rule set of 1000.
     */
    public void testLoadsOfOutboundRules() {
        // turn off logging
        Log.setLevel("ERROR");

        float testAmount = 10000; // number of times to run test
        float timePerRule = 3;  // ms per rule

        // test with 1000 rules
        Conf conf = new Conf();
        for (int i = 0; i < 1000; i++) {
            OutboundRule rule = new OutboundRule();
            rule.setFrom("^/([a-z]+)/([0-9]+)/" + i + "/$");
            rule.setTo("/blah/a/$2/");
            conf.addOutboundRule(rule);
        }
        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/dir/999/45/");
        // warm up
        UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);
        urlRewriteWrappedResponse.encodeURL("/aaa");

        long timeStart = System.currentTimeMillis();
        for (float i = 0; i < testAmount; i++) {
            urlRewriteWrappedResponse.encodeURL("/sdasd/asdasd/asdasd");
            if (i % 500 == 0 && i > 0) {
                System.out.println("avg so far " + ((System.currentTimeMillis() - timeStart) / i) + "ms per rule");
            }
        }
        long took = System.currentTimeMillis() - timeStart;
        System.out.println("took " + took + "ms " + (took / testAmount) + "ms per rule");
        assertTrue("should take less than " + timePerRule + "ms per rule", (took / testAmount) < timePerRule);
    }



}
