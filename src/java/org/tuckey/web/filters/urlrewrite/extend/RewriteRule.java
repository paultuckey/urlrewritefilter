package org.tuckey.web.filters.urlrewrite.extend;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A RewriteRule is basically the class that will figure out the answer to "Can we service this http request?".  If we
 * can we return a match object that will actually service the request.
 */
public class RewriteRule {

    /**
     * Initialise the rule.
     * If you return false here the rule will be marked as disabled.
     */
    public boolean initialise(ServletContext servletContext) {
        // do nothing
        return true;
    }

    /**
     * If other Rules should be ignored then return false,
     * return true if another Rule may "steal" the current request.
     * if not overriden then false is assumed.
     */

    /**
     * Defines if UrlRewrite engine will allow the request to go down to the actual resource.
     *
     * ie, if true a request for /blah/img.gif would only filter the request and /blah/img.gif would still be loaded
     * from the filesystem by the app server.
     *
     * if false this rule or match object will handle the response.  If this is not overriden false will be assumes.
     */


    /**
     * Prepare to be shut down.  There will be no more call to "matches" after this is called.
     */
    public void destroy() {
        // do nothing
    }

    /**
     * Can we match the current request?  null means no.  Return a valid RuleMatch object or
     * new SimpleRewriteMatch() (or an object that extends RewriteMatch) if this rule matches.
     *
     * @param request
     * @param response
     */
    public RewriteMatch matches(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

}
