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
package org.tuckey.web.filters.urlrewrite.test;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.FilterChain;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * RunObject that can be used for testing.  Included here and not in test folder as it needs to be deployed with the
 * main library.
 *
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
        return new MockRewriteMatch();
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
