package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;

import org.tuckey.web.testhelper.MockRequest;

/**
 * @author Tim Morrow
 * @since Dec 4, 2007
 */
public class VariableReplacerTest extends TestCase {

    private MockRequest request;
    
    protected void setUp() {
        request = new MockRequest();
    }

    public final void testReplaceNullValue() {
        request.getSession(true);
        final String result = VariableReplacer.replace("%{session-attribute:color}", request);
        
        assertEquals("", result);
    }

    public final void testReplace() {
        request.getSession(true).setAttribute("color", "red");
        final String result = VariableReplacer.replace("%{session-attribute:color}", request);
        
        assertEquals("red", result);
    }

    public final void testReplace2() {
        request.getSession(true).setAttribute("color", "red");
        final String result = VariableReplacer.replace("abcd$s%{session-attribute:color}efg", request);

        assertEquals("abcd$sredefg", result);
    }

    public final void testReplaceWithDollar() {
        request.getSession(true).setAttribute("color", "ab$cd");
        final String result = VariableReplacer.replace("%{session-attribute:color}", request);
        
        assertEquals("ab$cd", result);
    }

    public final void testReplaceWithBackslash() {
        request.getSession(true).setAttribute("color", "ab\\cd");
        final String result = VariableReplacer.replace("%{session-attribute:color}", request);
        
        assertEquals("ab\\cd", result);
    }

}
