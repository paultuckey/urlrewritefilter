package org.tuckey.web.filters.urlrewrite;

import org.junit.Before;
import org.junit.Test;
import org.tuckey.web.filters.urlrewrite.utils.RewriteUtils;
import org.tuckey.web.testhelper.MockFilterChain;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;

import static org.junit.Assert.*;

public class TestUtfRewriting {
    MockResponse response;
    MockRequest request;

    private String to = "/красота-от-природы/cловарь-ингредиентов/";
    private String from = "/красота-от-природы";

    MockFilterChain chain;

    @Before
    public void setUp() {

        response = new MockResponse();
        request = new MockRequest();
        chain = new MockFilterChain();

    }


    @Test
    public void testSimple() throws Exception {

        NormalRule rule = new NormalRule();
        rule.setFrom("/products");
        rule.setTo(to);
        rule.initialise(null);
        MockRequest request = new MockRequest("/products");
        final String requestURI = request.getRequestURI();
        RewrittenUrl rewrittenUrl = rule.matches(requestURI, request, response);
        assertEquals(to, rewrittenUrl.getTarget());

    }

    @Test
    public void testSimple2() throws Exception {

        NormalRule rule = new NormalRule();
        rule.setFrom(from);

        rule.setToType("permanent-redirect");
        rule.setTo(to);
        rule.initialise(null);
        MockRequest request = new MockRequest(from);
        final String requestURI = request.getRequestURI();
        RewrittenUrl rewrittenUrl = rule.matches(requestURI, request, response);
        assertEquals(to, rewrittenUrl.getTarget());
    }


    @Test
    public void testRedirect1() throws Exception {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl(to);
        rewrittenUrl.setTemporaryRedirect(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertEquals(RewriteUtils.uriEncodeParts(to), response.getHeader("Location"));
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());

    }

    @Test
    public void testRedirect2() throws Exception {
        final String target = "/Pomegranate+natural+beauty";
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl(target);
        rewrittenUrl.setTemporaryRedirect(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertEquals(RewriteUtils.uriEncodeParts(target), response.getHeader("Location"));
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());

    }
}
