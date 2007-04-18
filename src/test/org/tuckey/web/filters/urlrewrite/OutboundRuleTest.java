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
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;

/**
 * @author Paul Tuckey
 * @version $Revision: 52 $ $Date: 2007-02-26 07:00:28 +1300 (Mon, 26 Feb 2007) $
 */
public class OutboundRuleTest extends TestCase {

    MockResponse response;
    MockRequest request;

    public void setUp() {
        Log.setLevel("DEBUG");
        response = new MockResponse();
        request = new MockRequest();
    }


    public void testOutboundQueryStr() {
        Conf conf = new Conf();
        OutboundRule rule1 = new OutboundRule();
        rule1.setFrom("^/jsp-examples/cal/links.jsp\\?id=([0-9]+)");
        rule1.setTo("/jsp-examples/cal/links/$1");
        conf.addOutboundRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        MockRequest request = new MockRequest("/jsp-examples/cal/links.jsp?id=46");
        UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);

        assertEquals("/jsp-examples/cal/links/46;mockencoded=test", urlRewriteWrappedResponse.encodeURL("/jsp-examples/cal/links.jsp?id=46"));

    }

    public void testOutboundQueryStr2() {
        Conf conf = new Conf();
        OutboundRule rule1 = new OutboundRule();
        rule1.setFrom("^/storeitem.html\\?vid=20060621001&amp;iid=([0-9]+)&amp;cid=([0-9]+)$");
        rule1.setTo("/storeitem/id$1/c$2");
        conf.addOutboundRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);
        assertEquals("/storeitem/id666/c555;mockencoded=test", urlRewriteWrappedResponse.encodeURL("/storeitem.html?vid=20060621001&amp;iid=666&amp;cid=555"));

    }

    public void testOutboundQueryStr3() {
        Conf conf = new Conf();
        OutboundRule rule1 = new OutboundRule();
        rule1.setFrom("^/world.jsp?country=([a-z]+)&amp;city=([a-z]+)$");
        rule1.setTo("/world/$1/$2");
        conf.addOutboundRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);
        assertEquals("/storeitem/id666/c555;mockencoded=test", urlRewriteWrappedResponse.encodeURL("/world.jsp?country=usa&city=nyc"));

    }


    public void testOutbound2() {
        // check mockencoded is being added
        UrlRewriteWrappedResponse urlRewriteWrappedResponse2 = new UrlRewriteWrappedResponse(response, request, null);
        assertEquals("a.jsp;mockencoded=test?aaa=bbb", urlRewriteWrappedResponse2.encodeURL("a.jsp?aaa=bbb"));

        // check we can strip it
        Conf conf = new Conf();
        OutboundRule rule1 = new OutboundRule();
        rule1.setFrom("^(.*);mockencoded=.*?(\\?.*)?$");
        rule1.setTo("$1$2");
        rule1.setEncodeFirst(true);
        conf.addOutboundRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);
        assertEquals("a.jsp?aaa=bbb", urlRewriteWrappedResponse.encodeURL("a.jsp?aaa=bbb"));
        assertEquals("a.jsp", urlRewriteWrappedResponse.encodeURL("a.jsp"));
    }

    /**
     * To: UrlRewrite <urlrewrite@googlegroups.com>
     * Date: Sep 9, 2005 9:07 AM
     * Subject: Re: Hiding jsessionid from Google Bot (or everything!)
     * this rule
     * <p/>
     * <outbound-rule encodefirst="true">
     * <from>^(.*);jsessionid=.*(\?.*)$</from>
     * <to>$1$2</to>
     * </outbound-rule>
     * <p/>
     * with the following three links
     * <p/>
     * <A <%=response.encodeURL("/boo.jsp?somethingloi=1")%></a>
     * <br />
     * <%=response.encodeURL("/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1")%>
     * <br />
     * <%=response.encodeURL("http://localhost:8080/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1")%>
     * <p/>
     * with the following output
     * <p/>
     * /boo.jsp;jsessionid=93DDACD0EB60D0B6301008A262DC58E4?somethingloi=1
     * /boo.jsp?somethingloi=1
     * http://localhost:8080/boo.jsp?somethingloi=1
     * <p/>
     * The last 2 outputs, where I've manually included the session id give
     * the rewrite I want, but the other one won't - regardless of the
     * encodefirst setting.
     * <p/>
     * My thinking is that encodefirst="true" attribue isn't behaving as
     * expected.
     */
    public void testOutboundJsess() {
        // check mockencoded is being added

        UrlRewriteWrappedResponse urlRewriteWrappedResponse2 = new UrlRewriteWrappedResponse(response, request, null);

        assertEquals("/boo.jsp;mockencoded=test?somethingloi=1",
                urlRewriteWrappedResponse2.encodeURL("/boo.jsp?somethingloi=1"));
        assertEquals("/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0;mockencoded=test?somethingloi=1",
                urlRewriteWrappedResponse2.encodeURL("/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1"));
        assertEquals("http://localhost:8080/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1",
                urlRewriteWrappedResponse2.encodeURL("http://localhost:8080/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1"));

        // check we can strip it
        Conf conf = new Conf();
        OutboundRule rule1 = new OutboundRule();
        rule1.setFrom("^(?![a-z]+:)(.*);jsessionid=.*?([\\?|\\#].*)?$");
        rule1.setTo("$1$2");
        rule1.setEncodeFirst(true);
        conf.addOutboundRule(rule1);
        conf.initialise();

        UrlRewriter urlRewriter = new UrlRewriter(conf);

        UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);

        assertEquals("/boo.jsp;mockencoded=test",
                urlRewriteWrappedResponse.encodeURL("/boo.jsp"));
        assertEquals("/boo.jsp;mockencoded=test?somethingloi=1",
                urlRewriteWrappedResponse.encodeURL("/boo.jsp?somethingloi=1"));
        assertEquals("/boo.jsp?somethingloi=1",
                urlRewriteWrappedResponse.encodeURL("/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1"));
        assertEquals("http://localhost:8080/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1",
                urlRewriteWrappedResponse.encodeURL("http://localhost:8080/boo.jsp;jsessionid=91C4977F91B3CF69D2085B1B42BD5EB0?somethingloi=1"));

    }

    //todo: test multiple outbound rules with encodefirst on and off
}

//      "^/dir/([\\&a-zA-Z0-9\\s\\+\\/\\%&amp;]+).([a-zA-Z\\s\\+&amp;\\/]+).([a-zA-Z\\s]{2,})$"
