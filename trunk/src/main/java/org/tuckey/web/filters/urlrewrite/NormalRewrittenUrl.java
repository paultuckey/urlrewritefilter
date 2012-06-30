/**
 * Copyright (c) 2005-2007, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.tuckey.web.filters.urlrewrite;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private boolean proxy = false;
    private String target;
    private boolean encode;
    private boolean stopFilterChain = false;
    private boolean noSubstitution = false;
    private RewriteMatch rewriteMatch;
    private ServletContext targetContext = null;

    /**
     * Holds information about the rewirtten url.
     *
     * @param ruleExecutionOutput the url to rewrite to
     */
    public NormalRewrittenUrl(RuleExecutionOutput ruleExecutionOutput) {
        this.target = ruleExecutionOutput.getReplacedUrl();
        this.targetContext = ruleExecutionOutput.getReplacedUrlContext();
        this.stopFilterChain = ruleExecutionOutput.isStopFilterMatch();
        this.rewriteMatch = ruleExecutionOutput.getRewriteMatch();
        this.noSubstitution = ruleExecutionOutput.isNoSubstitution();
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

    public boolean isProxy() {
        return proxy;
    }

    public void setProxy(boolean proxy) {
        this.proxy = proxy;
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
        if (rewriteMatch != null) {
            // todo: exception handling?
            rewriteMatch.execute(hsRequest, hsResponse);
        }
        if (stopFilterChain) {
            // if we need to stop the filter chain don't do anything
            log.trace("stopping filter chain");
            requestRewritten = true;

        } else if (isNoSubstitution()) {
            log.trace("no substitution");
            requestRewritten = false;

        } else if (isForward()) {
            if (hsResponse.isCommitted()) {
                log.error("response is comitted cannot forward to " + target +
                        " (check you haven't done anything to the response (ie, written to it) before here)");
            } else {
          		final RequestDispatcher rq = getRequestDispatcher(hsRequest, target, targetContext);
                rq.forward(hsRequest, hsResponse);
                if (log.isTraceEnabled()) log.trace("forwarded to " + target);
            }
            requestRewritten = true;

        } else if (isPreInclude()) {
      		final RequestDispatcher rq = getRequestDispatcher(hsRequest, target, targetContext);
            rq.include(hsRequest, hsResponse);
            chain.doFilter(hsRequest, hsResponse);
            requestRewritten = true;
            if (log.isTraceEnabled()) log.trace("preinclded " + target);

        } else if (isPostInclude()) {
      		final RequestDispatcher rq = getRequestDispatcher(hsRequest, target, targetContext);
            chain.doFilter(hsRequest, hsResponse);
            rq.include(hsRequest, hsResponse);
            requestRewritten = true;
            if (log.isTraceEnabled()) log.trace("postinclded " + target);

        } else if (isRedirect()) {
            if (hsResponse.isCommitted()) {
                log.error("response is comitted cannot redirect to " + target +
                        " (check you haven't done anything to the response (ie, written to it) before here)");
            } else {
                if (isEncode()) {
                    target = hsResponse.encodeRedirectURL(target);
                }
                hsResponse.sendRedirect(target);
                if (log.isTraceEnabled()) log.trace("redirected to " + target);
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
                if (log.isTraceEnabled()) log.trace("temporarily redirected to " + target);
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
                if (log.isTraceEnabled()) log.trace("permanently redirected to " + target);
            }
            requestRewritten = true;

        } else if (isProxy()) {
            if (hsResponse.isCommitted()) {
                log.error("response is committed. cannot proxy " + target + ". Check that you havn't written to the response before.");
            } else {
                RequestProxy.execute(target, hsRequest, hsResponse);
                if (log.isTraceEnabled()) {
                    log.trace("Proxied request to " + target);
                }
            }
            requestRewritten = true;
        }
        return requestRewritten;
    }

    private RequestDispatcher getRequestDispatcher(final HttpServletRequest hsRequest, String toUrl,
                                                   ServletContext targetContext) throws ServletException {
        final RequestDispatcher rq = (targetContext != null) ? targetContext.getRequestDispatcher(target) : hsRequest.getRequestDispatcher(toUrl);
        if (rq == null) {
            // this might be a 404 possibly something else, could re-throw a 404 but is best to throw servlet exception
            throw new ServletException("unable to get request dispatcher for " + toUrl);
        }
        return rq;
    }

    /**
     * @return the targetContext
     */
    public ServletContext getTargetContext() {
        return targetContext;
    }

    /**
     * @param targetContext the targetContext to set
     */
    public void setTargetContext(ServletContext targetContext) {
        this.targetContext = targetContext;
    }

    public boolean isNoSubstitution() {
        return noSubstitution;
    }

    public void setNoSubstitution(boolean noSubstitution) {
        this.noSubstitution = noSubstitution;
    }


}
