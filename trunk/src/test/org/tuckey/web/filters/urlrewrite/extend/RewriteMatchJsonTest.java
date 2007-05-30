package org.tuckey.web.filters.urlrewrite.extend;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.Run;

import javax.servlet.ServletException;
import java.io.StringWriter;
import java.io.IOException;


public class RewriteMatchJsonTest extends TestCase {

    public void testJson() throws ServletException, IOException {
        RewriteMatchJson rmj = new RewriteMatchJson("hello");

        StringWriter sw = new StringWriter();
        rmj.writeJsonObject("hello", sw);
        assertEquals("{\"result\":\"hello\",\"id\":0}", sw.toString());

        StringWriter sw2 = new StringWriter();
        rmj.writeJsonObject(new Long(99), sw2);
        assertEquals("{\"result\":99,\"id\":0}", sw2.toString());

    }

    public void testJson3() throws ServletException, IOException {
        RewriteMatchJson rmj = new RewriteMatchJson("hello");
        StringWriter sw3 = new StringWriter();
        rmj.writeJsonObject(new Run(), sw3);
        assertEquals("g{\"result\":{\"newEachTime\":false," +
                "\"javaClass\":\"org.tuckey.web.filters.urlrewrite.Run\"," +
                "\"handler\":\"standard\",\"methodSignature\":\"run\"," +
                "\"methodStr\":\"run\",\"valid\":false,\"error\":null," +
                "\"id\":0,\"filter\":false,\"initialised\":false," +
                "\"classStr\":null,\"runClassInstance\":null," +
                "\"displayName\":\"Run 0\"},\"id\":0}", sw3.toString());
    }

    public void testJsonError() throws ServletException, IOException {
        RewriteMatchJson rmj = new RewriteMatchJson("hello");
        StringWriter sw3 = new StringWriter();
        rmj.writeJsonObject(new NullPointerException(), sw3);
        assertEquals("{\"error\":{\"code\":490," +
                "\"trace\":\"java.lang.NullPointerException\\r\\n\\t" +
                "at org.tuckey.web.filters.urlrewrite.extend.RewriteMatchJsonTest.testJsonError(RewriteMatchJsonTest.java:42)\\r\\n\\t" +
                "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\r\\n\\t" +
                "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\\r\\n\\t" +
                "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\\r\\n\\t" +
                "at java.lang.reflect.Method.invoke(Method.java:585)\\r\\n\\tat junit.framework.TestCase.runTest(TestCase.java:154)\\r\\n\\t" +
                "at junit.framework.TestCase.runBare(TestCase.java:127)\\r\\n\\tat junit.framework.TestResult$1.protect(TestResult.java:106)\\r\\n\\t" +
                "at junit.framework.TestResult.runProtected(TestResult.java:124)\\r\\n\\tat junit.framework.TestResult.run(TestResult.java:109)\\r\\n\\t" +
                "at junit.framework.TestCase.run(TestCase.java:118)\\r\\n\\tat junit.textui.TestRunner.doRun(TestRunner.java:116)\\r\\n\\t" +
                "at com.intellij.rt.execution.junit.IdeaTestRunner.doRun(IdeaTestRunner.java:65)\\r\\n\\t" +
                "at junit.textui.TestRunner.doRun(TestRunner.java:109)\\r\\n\\t" +
                "at com.intellij.rt.execution.junit.IdeaTestRunner.startRunnerWithArgs(IdeaTestRunner.java:24)\\r\\n\\t" +
                "at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:118)\\r\\n\\t" +
                "at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:40)\\r\\n\\t" +
                "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\\r\\n\\t" +
                "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\\r\\n\\t" +
                "at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\\r\\n\\t" +
                "at java.lang.reflect.Method.invoke(Method.java:585)\\r\\n\\t" +
                "at com.intellij.rt.execution.application.AppMain.main(AppMain.java:90)\\r\\n\"},\"id\":0}", sw3.toString());
    }

}
