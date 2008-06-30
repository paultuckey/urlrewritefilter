package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


public class FunctionReplacerTest  extends TestCase {

    public void setUp() {
        Log.setLevel("DEBUG");
    }

    public void testSimple1() throws InvocationTargetException, IOException, ServletException {
        assertTrue(FunctionReplacer.containsFunction("a${lower:HEllo}b"));
        assertEquals("ahellob", FunctionReplacer.replace("a${lower:HElLO}b"));
    }

    public void testSimple2() throws InvocationTargetException, IOException, ServletException {
        assertTrue(FunctionReplacer.containsFunction("a${upper:HEllo}b"));
        assertEquals("aHELLOb", FunctionReplacer.replace("a${upper:hellO}b"));
    }

    public void testSimple3() throws InvocationTargetException, IOException, ServletException {
        assertTrue(FunctionReplacer.containsFunction("a${replace:a b c: :_}b"));
        assertEquals("aa_b_cb", FunctionReplacer.replace("a${replace:a b c: :_}b"));
    }

    public void testSimple4() throws InvocationTargetException, IOException, ServletException {
        assertTrue(FunctionReplacer.containsFunction("a${replaceFirst:a b c: :_}b"));
        assertEquals("aa_b cb", FunctionReplacer.replace("a${replaceFirst:a b c: :_}b"));
    }

    public void testSimple5() throws InvocationTargetException, IOException, ServletException {
        assertTrue(FunctionReplacer.containsFunction("a${escape:a b c} b"));
        assertEquals("aa+b+c b", FunctionReplacer.replace("a${escape:a b c} b"));
    }

    public void testSimple6() throws InvocationTargetException, IOException, ServletException {
        assertTrue(FunctionReplacer.containsFunction("a${trim: b } b"));
        assertEquals("ab b", FunctionReplacer.replace("a${trim: b } b"));
    }

    public void testSimple7() throws InvocationTargetException, IOException, ServletException {
        assertTrue(FunctionReplacer.containsFunction("a${length:asdf} b"));
        assertEquals("a4 b", FunctionReplacer.replace("a${length:asdf} b"));
    }


}
