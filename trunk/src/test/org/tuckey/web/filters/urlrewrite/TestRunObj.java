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

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.testhelper.MockRewriteMatch;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Paul Tuckey
 * @version $Revision: 33 $ $Date: 2006-09-12 16:41:56 +1200 (Tue, 12 Sep 2006) $
 */
public class TestRunObj {
    private static boolean runCalled;
    private static boolean destroyCalled;
    private static boolean initCalled;
    private static boolean nonDefaultRunCalled;
    private static ServletConfig servletConfig;

    private static int createdCount = 0;
    private static String paramStr = null;
    private static long runWithChainParamAfterDoFilter = 0;

    public TestRunObj() {
        createdCount++;
    }

    public void run(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        runCalled = true;

        PrintWriter sos = httpServletResponse.getWriter();
        if (sos == null) return;
        sos.print("this is " + TestRunObj.class.getName());
        sos.close();
    }


    public MockRewriteMatch runWithReturnedObj(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        return new MockRewriteMatch();
    }

    public void nonDefaultRun(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        nonDefaultRunCalled = true;
    }

    public String runThatReturns(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        return "aaabbb";
    }

    public String runWithParam(int i)
            throws ServletException, IOException {
        paramStr = "" + i;
        return paramStr;
    }

    public String runWithNoParams()
            throws ServletException, IOException {
        paramStr = "[no params]";
        return paramStr;
    }

    public String runWithChainParam(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain c)
            throws ServletException, IOException {
        paramStr = "" + c;
        c.doFilter(httpServletRequest, httpServletResponse);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            //
        }
        runWithChainParamAfterDoFilter = System.currentTimeMillis();
        return paramStr;
    }

    public static long getRunWithChainParamAfterDoFilter() {
        return runWithChainParamAfterDoFilter;
    }

    public String runWithPrimitiveParam(int i, char c, double d, float f, short s, byte b, boolean b2, String s2)
            throws ServletException, IOException {
        paramStr = i + "," + c + "," + d + "," + f + "," + s + "," + b + "," + b2 + "," + s2;
        return paramStr;
    }

    public String runWithObjParam(Integer i, Character c, Double d, Float f, Short s, Byte b, Boolean b2, String s2)
            throws ServletException, IOException {
        paramStr = i + "," + c + "," + d + "," + f + "," + s + "," + b + "," + b2 + "," + s2;
        return paramStr;
    }

    public static String getParamStr() {
        return paramStr;
    }

    public void runNullPointerException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        exceptionGenerator.doNullPointer();
    }

    public void runRuntiumeException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        exceptionGenerator.doRuntime();
    }

    public void runServletException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        exceptionGenerator.doServlet();
    }

    public void runIOException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        exceptionGenerator.doIO();
    }

    public void runCustomException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws TestExceptionGenerator.CustomException {
        exceptionGenerator.doCustom();
    }

    TestExceptionGenerator exceptionGenerator = new TestExceptionGenerator();

    private class TestExceptionGenerator {
        public void doNullPointer() {
            String aaa = null;
            // YES we WANT a null pointer here
            aaa.toLowerCase();
        }
        public void doRuntime() {
            throw new RuntimeException("shit!");
        }
        public void doServlet() throws ServletException {
            throw new ServletException("serv");
        }
        public void doIO() throws IOException {
            throw new IOException("me i.o. has gone crazy");
        }
        public void doCustom() throws CustomException {
            throw new CustomException();
        }
        public class CustomException extends Exception {

        }
    }

    public RewriteMatch trialException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                       ClassNotFoundException e) {
        return new TestRewriteMatch();
    }

    /**
     * Do not delete! used in RunTest.
     */
    private void privateRun(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        // do nothing
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
