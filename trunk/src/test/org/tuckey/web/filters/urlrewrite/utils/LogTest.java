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
package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;
import org.tuckey.web.testhelper.MockFilterConfig;

/**
 * 
 * 
 */
public class LogTest extends TestCase {

    public void testInitNull() {
        Log.setConfiguration(null);
    }

    public void testInitEmpty() {
        Log.setConfiguration(new MockFilterConfig());
    }

    public void testNull() {
        Log log = Log.getLog(null);
        log.debug("hi");
    }

    public void testDebug() {
        Log log = Log.getLog(null);
        Log.setLevel("DEBUG");
        assertFalse(log.isTraceEnabled());
        assertTrue(log.isDebugEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        log.debug("hi");
    }

    public void testLevelCase() {
        Log log = Log.getLog(null);
        Log.setLevel("SysOut:DEbug");
        assertTrue("debug", log.isDebugEnabled());
        assertTrue("std out", log.isUsingSystemOut());
        log.debug("hi");
    }

    public void testLevelCaseErr() {
        Log log = Log.getLog(null);
        Log.setLevel("StdErR:DEbug");
        assertTrue("debug", log.isDebugEnabled());
        assertTrue("std err", log.isUsingSystemErr());
        log.debug("hi err");
    }
}
