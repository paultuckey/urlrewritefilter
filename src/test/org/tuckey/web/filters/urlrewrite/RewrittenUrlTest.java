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
import org.tuckey.web.testhelper.MockFilterChain;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockRequestDispatcher;
import org.tuckey.web.testhelper.MockResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Paul Tuckey
 * @version $Revision: 40 $ $Date: 2006-10-27 15:12:37 +1300 (Fri, 27 Oct 2006) $
 */
public class RewrittenUrlTest extends TestCase {

    MockResponse response;
    MockRequest request;
    MockFilterChain chain;

    public void setUp() {
        Log.setLevel("DEBUG");
        response = new MockResponse();
        request = new MockRequest();
        chain = new MockFilterChain();
    }

    public void testPermanentRedir() throws IOException, ServletException {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
        rewrittenUrl.setPermanentRedirect(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertEquals("/hi", response.getHeader("Location"));
        assertEquals(HttpServletResponse.SC_MOVED_PERMANENTLY, response.getStatus());
    }

    public void testTempRedir() throws IOException, ServletException {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
        rewrittenUrl.setTemporaryRedirect(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertEquals("/hi", response.getHeader("Location"));
        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
    }

    public void testForward() throws IOException, ServletException {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
        rewrittenUrl.setForward(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertEquals("/hi", MockRequestDispatcher.getCurrent().getUrl());
        assertTrue(MockRequestDispatcher.getCurrent().isForwarded());
    }

    public void testRedir() throws IOException, ServletException {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
        rewrittenUrl.setRedirect(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertEquals("/hi", response.getRedirectedUrl());
    }

    public void testRedirEncoded() throws IOException, ServletException {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
        rewrittenUrl.setRedirect(true);
        rewrittenUrl.setEncode(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertEquals("/hi;mockencoded=test", response.getRedirectedUrl());
    }

    public void testPreIncl() throws IOException, ServletException {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
        rewrittenUrl.setPreInclude(true);
        rewrittenUrl.setEncode(true);
        rewrittenUrl.doRewrite(request, response, chain);
        assertTrue(MockRequestDispatcher.getCurrent().isIncluded() && chain.isDoFilterRun());
    }

    public void testPostIncl() throws IOException, ServletException {
        NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
        rewrittenUrl.setPostInclude(true);
        rewrittenUrl.setEncode(true);
        rewrittenUrl.doRewrite(request, response, chain);
        // todo: figure out how to ensure this is actually post
        assertTrue(MockRequestDispatcher.getCurrent().isIncluded() && chain.isDoFilterRun());
    }


}
