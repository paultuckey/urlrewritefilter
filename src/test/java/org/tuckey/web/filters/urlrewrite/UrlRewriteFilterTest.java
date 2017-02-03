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
import org.tuckey.web.testhelper.MockFilterConfig;
import org.tuckey.web.testhelper.MockServletContext;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.ServletException;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class UrlRewriteFilterTest extends TestCase {

    private UrlRewriteFilter filter;

    public void setUp() {
        Log.setLevel("stdout:TRACE");
        filter = new UrlRewriteFilter();
        Log.setLevel("stdout:TRACE");
    }

    public void tearDown() {
        filter.destroy();
        filter = null;
    }

    public void testInit() throws ServletException {
        filter.init(null);
        filter.init(new MockFilterConfig());
    }

    public void testVersion() throws ServletException {
        String ver = UrlRewriteFilter.getFullVersionString();
        System.out.println(ver);
        assertTrue("Ver bad " + ver, ver.matches("[0-9]+\\.[0-9]+\\.[0-9]+(-SNAPSHOT)? build [0-9a-z]+"));
    }

    public void testInitContext() throws ServletException {
        MockFilterConfig mockFilterConfig = new MockFilterConfig();
        mockFilterConfig.setServletContext(new MockServletContext());
        filter.init(mockFilterConfig);
    }

}
