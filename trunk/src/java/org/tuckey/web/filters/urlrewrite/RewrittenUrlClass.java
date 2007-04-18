package org.tuckey.web.filters.urlrewrite;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * A rewrite target that is acually rewriting the url to a class a user has specified.
 */
class RewrittenUrlClass implements RewrittenUrl {
        RewriteMatch rewriteMatch;
        private String matchingUrl;

        protected RewrittenUrlClass(RewriteMatch rewriteMatch) {
            this.matchingUrl = rewriteMatch.getMatchingUrl();
            this.rewriteMatch = rewriteMatch;
        }

        public boolean doRewrite(final HttpServletRequest hsRequest,
                                 final HttpServletResponse hsResponse, final FilterChain chain)
                throws IOException, ServletException {

            return rewriteMatch.execute(hsRequest, hsResponse);
        }

        public String getTarget() {
            return matchingUrl;
        }

    }
