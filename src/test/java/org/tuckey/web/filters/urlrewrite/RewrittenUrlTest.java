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
        Log.setLevel("SYSOUT:TRACE");
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
        assertTrue(MockRequestDispatcher.getCurrent().isIncluded() && chain.isDoFilterRun());
        System.out.println("time diff: " + (MockRequestDispatcher.getCurrent().getIncludeLastCalled() - chain.getTimeInvoked()) + "ms" );
        assertTrue(chain.getTimeInvoked() < MockRequestDispatcher.getCurrent().getIncludeLastCalled());
    }

	public void testNoSubstitution() throws IOException, ServletException {
    	NormalRewrittenUrl rewrittenUrl = new NormalRewrittenUrl("/hi");
    	rewrittenUrl.setNoSubstitution(true);
    	assertFalse(rewrittenUrl.doRewrite(request, response, chain));
    }

}
