package org.tuckey.web.filters.urlrewrite.extend;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.FilterChain;
import java.io.IOException;

/**
 * Service the request with a clean object.
 * Perform any business logic data retrival etc then prepare an object for presentation of the data.
 */
public class RewriteMatch {

    /**
     * When future rules are processed they need to have a URL to compare against.  If this method is not implemented
     * then the url before this rule will be used. If isLast() returns true then this will never be called.
     */
    public String getMatchingUrl() {
        return null;
    }

    /**
     * If this rule has been matched and has not been "stolen" by another rule then process the request.
     *
     * If you return true then the filter chain will NOT continue.
     */
    public boolean execute(HttpServletRequest request,
                                 HttpServletResponse response) throws ServletException, IOException {
        // do nothing by default
        return true;
    }

}




