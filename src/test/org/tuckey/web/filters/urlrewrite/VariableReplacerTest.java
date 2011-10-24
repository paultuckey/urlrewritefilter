package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.substitution.VariableReplacer;
import org.tuckey.web.testhelper.MockRequest;

import javax.servlet.ServletContext;

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

    public final void testQueryStringValue() {
        request.setQueryString("keyword=$2");
        final String result = VariableReplacer.replace("%{query-string}", request);
        assertEquals("keyword=$2", result);
    }

    public final void testNonRecursiveSubstitution() {
        request.setQueryString("keyword=%{query-string}");
        final String result = VariableReplacer.replace("%{query-string}", request);
        assertEquals("keyword=%{query-string}", result);
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

    public final void testReplaceContextVars() {
    	ServletContext servletContext = request.getSession(true).getServletContext();
    	servletContext.setAttribute("host", "http://testurl");    	
    	//request.getMockSession().setServletContext(servletContext);
    	
        final String result = VariableReplacer.replaceWithServletContext("%{context:host}", request, servletContext);

        assertEquals("http://testurl", result);
    }

    
}
