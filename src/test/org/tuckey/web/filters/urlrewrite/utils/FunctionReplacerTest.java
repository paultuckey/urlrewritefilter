package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.Function;
import org.tuckey.web.filters.urlrewrite.functions.StringFunctions;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;


public class FunctionReplacerTest  extends TestCase {

    public void setUp() {
        Log.setLevel("DEBUG");
    }

    public void testSimple1() throws InvocationTargetException, IOException, ServletException {
        Function function = new Function();
        function.setName("lower");
        function.setClassStr(StringFunctions.class.getName());
        function.setMethodStr("toLower");
        function.initialise(null);
        Map functions = new HashMap();
        functions.put(function.getName(), function);
        assertTrue(FunctionReplacer.containsFunction("a${lower:HEllo}b"));
        assertEquals("ahellob", FunctionReplacer.replace("a${lower:HElLO}b", functions, null));
    }

    public void testSimple2() throws InvocationTargetException, IOException, ServletException {
        Function function = new Function();
        function.setName("upper");
        function.setClassStr(StringFunctions.class.getName());
        function.setMethodStr("toUpper");
        function.initialise(null);
        Map functions = new HashMap();
        functions.put(function.getName(), function);
        assertTrue(FunctionReplacer.containsFunction("a${upper:HEllo}b"));
        assertEquals("aHELLOb", FunctionReplacer.replace("a${upper:hellO}b", functions, null));
    }

    public void testSimple3() throws InvocationTargetException, IOException, ServletException {
        Function function = new Function();
        function.setName("repl");
        function.setClassStr(StringFunctions.class.getName());
        function.setMethodStr("replaceAll");
        function.initialise(null);
        Map functions = new HashMap();
        functions.put(function.getName(), function);
        assertTrue(FunctionReplacer.containsFunction("a${repl:a b c: :_}b"));
        assertEquals("aa_b_cb", FunctionReplacer.replace("a${repl:a b c: :_}b", functions, null));
    }

    public void testSimple4() throws InvocationTargetException, IOException, ServletException {
        Function function = new Function();
        function.setName("repl");
        function.setClassStr(StringFunctions.class.getName());
        function.setMethodStr("replaceFirst");
        function.initialise(null);
        Map functions = new HashMap();
        functions.put(function.getName(), function);
        assertTrue(FunctionReplacer.containsFunction("a${repl:a b c: :_}b"));
        assertEquals("aa_b cb", FunctionReplacer.replace("a${repl:a b c: :_}b", functions, null));
    }


}
