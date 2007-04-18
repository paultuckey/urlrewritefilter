package org.tuckey.web.filters.urlrewrite.sample;

import org.tuckey.web.filters.urlrewrite.extend.RewriteRule;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A sample of how you might write a custom rule.
 */
public class SampleRewriteRule extends RewriteRule {


    public RewriteMatch matches(HttpServletRequest request, HttpServletResponse response) {

        // return null if we don't want the request
        if (!request.getRequestURI().startsWith("/staff/")) return null;

        // grab the things out of the url we need
        Integer id = Integer.valueOf(request.getRequestURI().replaceFirst("/staff/([0-9]+)/", "$1"));
        // if we don't get a good id then return null
        if (id == null) return null;

        // match required with clean parameters
        return new SampleRewriteMatch(id.intValue());
    }

}
