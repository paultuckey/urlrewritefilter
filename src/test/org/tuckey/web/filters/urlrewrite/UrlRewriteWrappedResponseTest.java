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
import org.tuckey.web.filters.urlrewrite.utils.Log;

/**
 * @author Paul Tuckey
 * @version $Revision: 12 $ $Date: 2006-08-20 20:53:09 +1200 (Sun, 20 Aug 2006) $
 */
public class UrlRewriteWrappedResponseTest extends TestCase {

    MockResponse response;

    public void setUp() {
        Log.setLevel("DEBUG");
        response = new MockResponse();
    }

    public void testUrlEncode() {
        Conf conf = new Conf();
        OutboundRule rule1 = new OutboundRule();
        rule1.setFrom("/aaa");
        rule1.setTo("/bbb");
        conf.addOutboundRule(rule1);
        conf.initialise();
        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("doesn't matter");
        UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);

        assertEquals("/bbb;mockencoded=test", urlRewriteWrappedResponse.encodeURL("/aaa"));

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

