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
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;
import org.tuckey.web.testhelper.MockServletContext;
import org.tuckey.web.testhelper.MockFilterChain;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * @author Paul Tuckey
 * @version $Revision: 40 $ $Date: 2006-10-27 15:12:37 +1300 (Fri, 27 Oct 2006) $
 */
public class RunTest extends TestCase {

    MockResponse response;
    MockRequest request;
    MockServletContext servletContext;

    public void setUp() {
        Log.setLevel("DEBUG");
        response = new MockResponse();
        request = new MockRequest();
        servletContext = new MockServletContext();
        TestRunObj.resetTestFlags();
    }

    public void testRun01() throws IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ServletException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.initialise(servletContext);
        assertTrue("Should be a initialised " + run.getError(), run.isValid());
        assertTrue("Should be created now", TestRunObj.getCreatedCount() == 1);
        assertTrue("Should be inited now", TestRunObj.isInitCalled());
        run.execute(request, response);
        assertTrue("Should be invoked", TestRunObj.isRunCalled());
        assertTrue("Should not have been created again", TestRunObj.getCreatedCount() == 1);
        assertFalse("Should not be destroyed", TestRunObj.isDestroyCalled());
        run.destroy();
        assertTrue("Should be destroyed", TestRunObj.isDestroyCalled());
    }

    public void testRunBadObj() throws IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ServletException {
        Run run = new Run();
        run.setClassStr("this.is.an.not.found.Class");
        run.initialise(servletContext);
        assertFalse("Should not be initialised " + run.getError(), run.isValid());
        run.execute(request, response);
        // Should not error just do nothing
    }

    public void testRunBadMethod() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("badMethod");
        run.initialise(servletContext);
        assertFalse("Should not be initialised " + run.getError(), run.isValid());
        run.execute(request, response);
        // Should not error just do nothing
    }

    public void testRunThatReturns() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("runThatReturns");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());
        run.execute(request, response);
        // Should not error just do nothing
    }

    public void testRunNoParams() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("runWithNoParams()");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());
        run.execute(request, response);
        // Should not error just do nothing
        assertEquals("[no params]", TestRunObj.getParamStr());
    }

    public void testRunCustomMethod() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("nonDefaultRun");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());
        run.execute(request, response);
        assertTrue("Should be invoked", TestRunObj.isNonDefaultRunCalled());
        // Should not error just do nothing
    }


    public void testRunMethodParams() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        // run.setMethodStr("runWithParam(  int, String, d, long, req, res  )");
        run.setMethodStr("runWithParam(  int )");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());
        run.execute(request, response, new Object[]{"99"} );
        assertEquals("Should be invoked", "99", TestRunObj.getParamStr());
        // Should not error just do nothing
    }

    public void testRunMethodParamNamed() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        // run.setMethodStr("runWithParam(  int, String, d, long, req, res  )");
        run.setMethodStr("runWithParam(  int  id )");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());
        request.setParameter("id", "99");
        run.execute(request, response);
        assertEquals("Should be invoked", "99", TestRunObj.getParamStr());
        // Should not error just do nothing
    }

    public void testRunWithChainParam() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        // run.setMethodStr("runWithParam(  int, String, d, long, req, res  )");
        run.setMethodStr("runWithChainParam( req, res, chain )");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());
        MockFilterChain chain = new MockFilterChain();

        run.execute(request, response, null, chain );
        assertEquals("Should have invoked once only", 1, chain.getInvocationCount());
        assertTrue(run.isFilter());
        // Should not error just do nothing
    }

    public void testParamsPrimitive() throws Exception {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("runWithPrimitiveParam(int,  char , double, float,short, byte , boolean , String)");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());

        assertEquals("runWithPrimitiveParam(int, char, double, float, short, byte, boolean, java.lang.String)", run.getMethodSignature());

        // check null's
        run.execute(request, response);
        assertEquals("0,\u0000,0.0,0.0,0,0,false,null", TestRunObj.getParamStr());

        // check values
        Object[] args = {"12", "xyz", "11", "11.58", "2", "1", "true", "abcd"};
        run.execute(request, response, args);
        assertEquals("12,x,11.0,11.58,2,1,true,abcd", TestRunObj.getParamStr());

    }


    public void testParamsObj() throws Exception {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("runWithObjParam(Integer , Character , Double , Float , Short , Byte , Boolean , String )");
        run.initialise(servletContext);
        assertTrue("Should be initialised " + run.getError(), run.isValid());

        assertEquals("runWithObjParam(java.lang.Integer, java.lang.Character, java.lang.Double, java.lang.Float, " +
                "java.lang.Short, java.lang.Byte, java.lang.Boolean, java.lang.String)", run.getMethodSignature());

        // check null's
        run.execute(request, response);
        assertEquals("null,null,null,null,null,null,null,null", TestRunObj.getParamStr());

        // check values
        Object[] args = {"12", "xyz", "11", "11.58", "2", "1", "true", "abcd"};
        run.execute(request, response, args);
        assertEquals("12,x,11.0,11.58,2,1,true,abcd", TestRunObj.getParamStr());

    }


    private Throwable doExceptionRun(String methodName) {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr(methodName);
        run.initialise(servletContext);
        assertTrue("Should be initialised, but: " + run.getError(), run.isValid());

        Throwable throwableViaRun = null;

        System.out.println("this...");
        try {
            run.execute(request, response);
        } catch (Throwable t) {
            throwableViaRun = t;
            t.printStackTrace(System.out);
        }
        //noinspection ConstantConditions
        return throwableViaRun.getCause();
    }

    public void testRunExceptionMethod() {
        Throwable throwableViaRun = doExceptionRun("runNullPointerException");
        Throwable throwableRaw = null;
        System.out.println("should look the same as this...");
        try {
            new TestRunObj().runNullPointerException(null, null);
        } catch (Throwable t) {
            throwableRaw = t;
            t.printStackTrace(System.out);
        }
        //noinspection ConstantConditions
        assertEquals(throwableRaw.toString(), throwableViaRun.toString());
    }

    public void testRunServletExceptionMethod() {
        Throwable throwableViaRun = doExceptionRun("runServletException");
        Throwable throwableRaw = null;
        System.out.println("should look the same as this...");
        TestRunObj testRunObj = new TestRunObj();
        try {
            testRunObj.runServletException(null, null);
        } catch (Throwable t) {
            throwableRaw = t;
            t.printStackTrace(System.out);
        }
        //noinspection ConstantConditions
        assertEquals(throwableRaw.toString(), throwableViaRun.toString());
    }

    public void testRunCustomExceptionMethod() {
        Throwable throwableViaRun = doExceptionRun("runCustomException");
        Throwable throwableRaw = null;
        System.out.println("should look the same as this...");
        TestRunObj testRunObj = new TestRunObj();
        try {
            testRunObj.runCustomException(null, null);
        } catch (Throwable t) {
            throwableRaw = t;
            t.printStackTrace(System.out);
        }
        //noinspection ConstantConditions
        assertEquals(throwableRaw.toString(), throwableViaRun.toString());
    }

    public void testRunIOExceptionMethod() {
        Throwable throwableViaRun = doExceptionRun("runIOException");
        Throwable throwableRaw = null;
        System.out.println("should look the same as this...");
        TestRunObj testRunObj = new TestRunObj();
        try {
            testRunObj.runIOException(null, null);
        } catch (Throwable t) {
            throwableRaw = t;
            t.printStackTrace(System.out);
        }
        //noinspection ConstantConditions
        assertEquals(throwableRaw.toString(), throwableViaRun.toString());
    }

    public void testRunRuntimeExceptionMethod() {
        Throwable throwableViaRun = doExceptionRun("runRuntiumeException");
        Throwable throwableRaw = null;
        System.out.println("should look the same as this...");
        TestRunObj testRunObj = new TestRunObj();
        try {
            testRunObj.runRuntiumeException(null, null);
        } catch (Throwable t) {
            throwableRaw = t;
            t.printStackTrace(System.out);
        }
        //noinspection ConstantConditions
        assertEquals(throwableRaw.toString(), throwableViaRun.toString());
    }

    public void testRunPrivateMethod() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("privateRun");
        run.initialise(servletContext);
        assertFalse("Should not be initialised " + run.getError(), run.isValid());
        run.execute(request, response);
        // Should not error just do nothing, check log msgs
    }

    public void testRunNewEach() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setNewEachTime(true);
        run.initialise(servletContext);
        assertTrue("Should not have been created yet", TestRunObj.getCreatedCount() == 0);
        run.execute(request, response);
        assertTrue("Should be created now", TestRunObj.getCreatedCount() == 1);
        run.execute(request, response);
        assertTrue("Should be created twice", TestRunObj.getCreatedCount() == 2);
        assertTrue("Should be destroyed", TestRunObj.isDestroyCalled());
    }

    public void testInitParams() {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.addInitParam("horse", "golden");
        run.addInitParam("debs", "nightout");
        run.initialise(servletContext);
        assertEquals("golden", TestRunObj.getTestServletConfig().getInitParameter("horse"));
        assertEquals("nightout", TestRunObj.getTestServletConfig().getInitParameter("debs"));
    }


    public void testRuleNoToWithRun() throws IOException, ServletException, InvocationTargetException {
        Run run = new Run();
        run.setClassStr(org.tuckey.web.filters.urlrewrite.TestRunObj.class.getName());
        run.setMethodStr("run");
        NormalRule rule = new NormalRule();
        rule.setFrom("from");
        rule.addRun(run);
        rule.initialise(null);
        MockRequest request = new MockRequest("from");

        RewrittenUrl rewrittenUrl = rule.matches(request.getRequestURI(), request, response);
        assertNull(rewrittenUrl);
        assertTrue(TestRunObj.isRunCalled());
    }


}
