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
package org.tuckey.web.filters.urlrewrite.json;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;

import javax.servlet.ServletException;
import java.io.IOException;


/**
 * Test JsonRewriteMatch.
 */
public class JsonRewriteMatchTest extends TestCase {

    public void testSimple() throws IOException, ServletException {
        Log.setLevel("SYSOUT:DEBUG");
        JsonRewriteMatch jsonRewriteMatch = new JsonRewriteMatch(null);
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        jsonRewriteMatch.execute(request, response);
        assertEquals("{\"result\":null}", response.getOutputStreamAsString());
    }

    public void testEcho() throws IOException, ServletException {
        Log.setLevel("SYSOUT:DEBUG");
        JsonRewriteMatch jsonRewriteMatch = new JsonRewriteMatch("Hello World");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        jsonRewriteMatch.execute(request, response);
        assertEquals("{\"result\":\"Hello World\"}", response.getOutputStreamAsString());
    }

    /**
     * @noinspection ThrowableInstanceNeverThrown
     */
    public void testError() throws IOException, ServletException {
        Log.setLevel("SYSOUT:DEBUG");
        JsonRewriteMatch jsonRewriteMatch = new JsonRewriteMatch(new Exception("Hello World"));
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        jsonRewriteMatch.execute(request, response);
        assertEquals("{\"error\":{\"error\":\"java.lang.Exception: Hello World\",\"message\":\"Hello World\",\"name\":\"java.lang.Exception\"}}", response.getOutputStreamAsString());
    }

}
