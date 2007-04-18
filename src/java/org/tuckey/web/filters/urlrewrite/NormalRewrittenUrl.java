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

import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.IOException;


/**
 * Holds information about the rewirtten url.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class NormalRewrittenUrl implements RewrittenUrl {

    private static Log log = Log.getLog(RewrittenUrl.class);

    private boolean forward = false;
    private boolean redirect = false;
    private boolean permanentRedirect = false;
    private boolean temporaryRedirect = false;
    private boolean preInclude = false;
    private boolean postInclude = false;
    private String target;
    private boolean encode;
    private boolean stopFilterChain = false;
    private RewriteMatch rewriteMatch;

    /**
     * Holds information about the rewirtten url.
     *
     * @param ruleExecutionOutput the url to rewrite to
     */
    public NormalRewrittenUrl(RuleExecutionOutput ruleExecutionOutput) {
        this.target = ruleExecutionOutput.getReplacedUrl();
        this.stopFilterChain = ruleExecutionOutput.isStopFilterMatch();
        this.rewriteMatch = ruleExecutionOutput.getRewriteMatch();
    }

    /**
     * Holds information about the rewirtten url.
     *
     * @param target the url to rewrite to
     */
    protected NormalRewrittenUrl(String target) {
        this.target = target;
    }

    /**
     * Gets the target url
     *
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    public boolean isForward() {
        return forward;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public void setPermanentRedirect(boolean permanentRedirect) {
        this.permanentRedirect = permanentRedirect;
    }

    public boolean isPermanentRedirect() {
        return permanentRedirect;
    }

    public void setTemporaryRedirect(boolean temporaryRedirect) {
        this.temporaryRedirect = temporaryRedirect;
    }

    public boolean isTemporaryRedirect() {
        return temporaryRedirect;
    }

    public void setEncode(boolean b) {
        encode = b;
    }

    public boolean isEncode() {
        return encode;
    }

    public boolean isPreInclude() {
        return preInclude;
    }

    public void setPreInclude(boolean preInclude) {
        this.preInclude = preInclude;
    }

    public boolean isPostInclude() {
        return postInclude;
    }

    public void setPostInclude(boolean postInclude) {
        this.postInclude = postInclude;
    }

    public boolean isStopFilterChain() {
        return stopFilterChain;
    }

    public void setStopFilterChain(boolean stopFilterChain) {
        this.stopFilterChain = stopFilterChain;
    }

    /**
     * The method that actually handles the outcome and rewrites.
     *
     * @param hsRequest
     * @param hsResponse
     * @param chain
     * @return True if the request was rewritten otherwise false.
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */
    public boolean doRewrite(final HttpServletRequest hsRequest,
                             final HttpServletResponse hsResponse, final FilterChain chain)
            throws IOException, ServletException {
        boolean requestRewritten = false;
        String target = getTarget();
        if (log.isTraceEnabled()) {
            log.trace("doRewrite called");
        }
        if ( rewriteMatch != null ) {
            // todo: exception handling?
            rewriteMatch.execute(hsRequest, hsResponse);
        }
        if (stopFilterChain) {
            // if we need to stop the filter chain don't do anything
            log.trace("stopping filter chain");
            requestRewritten = true;

        } else if (isForward()) {
            if (hsResponse.isCommitted()) {
                log.error("response is comitted cannot forward to " + target +
                        " (check you haven't done anything to the response (ie, written to it) before here)");
            } else {
                final RequestDispatcher rq = getRequestDispatcher(hsRequest, target);
                rq.forward(hsRequest, hsResponse);
                if ( log.isTraceEnabled() ) log.trace("forwarded to " + target);
            }
            requestRewritten = true;

        } else if (isPreInclude()) {
            final RequestDispatcher rq = getRequestDispatcher(hsRequest, target);
            rq.include(hsRequest, hsResponse);
            chain.doFilter(hsRequest, hsResponse);
            requestRewritten = true;
            if ( log.isTraceEnabled() ) log.trace("preinclded " + target);

        } else if (isPostInclude()) {
            final RequestDispatcher rq = getRequestDispatcher(hsRequest, target);
            chain.doFilter(hsRequest, hsResponse);
            rq.include(hsRequest, hsResponse);
            requestRewritten = true;
            if ( log.isTraceEnabled() ) log.trace("postinclded " + target);

        } else if (isRedirect()) {
            if (hsResponse.isCommitted()) {
                log.error("response is comitted cannot redirect to " + target +
                        " (check you haven't done anything to the response (ie, written to it) before here)");
            } else {
                if (isEncode()) {
                    target = hsResponse.encodeRedirectURL(target);
                }
                hsResponse.sendRedirect(target);
                if ( log.isTraceEnabled() ) log.trace("redirected to " + target);
            }
            requestRewritten = true;

        } else if (isTemporaryRedirect()) {
            if (hsResponse.isCommitted()) {
                log.error("response is comitted cannot temporary redirect to " + target +
                        " (check you haven't done anything to the response (ie, written to it) before here)");
            } else {
                if (isEncode()) {
                    target = hsResponse.encodeRedirectURL(target);
                }
                hsResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                hsResponse.setHeader("Location", target);
                if ( log.isTraceEnabled() ) log.trace("temporarily redirected to " + target);
            }
            requestRewritten = true;

        } else if (isPermanentRedirect()) {
            if (hsResponse.isCommitted()) {
                log.error("response is comitted cannot permanent redirect " + target +
                        " (check you haven't done anything to the response (ie, written to it) before here)");
            } else {
                if (isEncode()) {
                    target = hsResponse.encodeRedirectURL(target);
                }
                hsResponse.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                hsResponse.setHeader("Location", target);
                if ( log.isTraceEnabled() ) log.trace("permanently redirected to " + target);
            }
            requestRewritten = true;

        }
        return requestRewritten;
    }

    private RequestDispatcher getRequestDispatcher(final HttpServletRequest hsRequest, String toUrl) throws ServletException {
        final RequestDispatcher rq = hsRequest.getRequestDispatcher(toUrl);
        if (rq == null) {
            // this might be a 404 possibly something else, could re-throw a 404 but is best to throw servlet exception
            throw new ServletException("unable to get request dispatcher for " + toUrl);
        }
        return rq;
    }

}
