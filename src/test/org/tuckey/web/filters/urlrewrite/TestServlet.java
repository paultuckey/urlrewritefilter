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
