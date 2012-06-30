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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class TestServlet extends HttpServlet {
    private static final long serialVersionUID = 8797402387550458476L;

    private static boolean runCalled;
    private static boolean destroyCalled;
    private static boolean initCalled;
    private static boolean nonDefaultRunCalled;
    private static ServletConfig servletConfig;

    private static int createdCount = 0;

    public TestServlet() {
        createdCount++;
    }

    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        PrintWriter sos = httpServletResponse.getWriter();

        sos.print("this is doGet on " + TestServlet.class.getName());
        sos.close();
        runCalled = true;
    }

    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        PrintWriter sos = httpServletResponse.getWriter();

        sos.print("this is doPost on " + TestServlet.class.getName());
        sos.close();
        runCalled = true;
    }

    public void nonDefaultRun(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        nonDefaultRunCalled = true;
    }

    public void destroy() {
        destroyCalled = true;
    }

    public void init(ServletConfig config) throws ServletException {
        servletConfig = config;
        initCalled = true;
    }

    public static boolean isRunCalled() {
        return runCalled;
    }

    public static int getCreatedCount() {
        return createdCount;
    }

    public static void resetTestFlags() {
        createdCount = 0;
        runCalled = false;
        destroyCalled = false;
        initCalled = false;
        nonDefaultRunCalled = false;
        servletConfig = null;
    }

    public static ServletConfig getTestServletConfig() {
        return servletConfig;
    }

    public static boolean isDestroyCalled() {
        return destroyCalled;
    }

    public static boolean isInitCalled() {
        return initCalled;
    }

    public static boolean isNonDefaultRunCalled() {
        return nonDefaultRunCalled;
    }
}
