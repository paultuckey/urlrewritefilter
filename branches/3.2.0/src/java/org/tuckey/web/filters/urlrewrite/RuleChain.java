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

import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.HashMap;

/**
 * Chain of rules.  Implemented as a chain so that java rules can filter the request, resposne.
 *
 * @author Paul Tuckey
 * @version $Revision: 33 $ $Date: 2006-09-12 16:41:56 +1200 (Tue, 12 Sep 2006) $
 */
public class RuleChain implements FilterChain {

    private static Log log = Log.getLog(UrlRewriter.class);

    // if called then call continue to process rules and call chain as if nothing happened
    private int ruleIdxToRun = 0;
    private RewrittenUrl finalRewrittenRequest = null;
    private String finalToUrl;
    private List rules;
    private boolean requestRewritten;
    private boolean rewriteHandled = false;
    private boolean responseHandled;
    private FilterChain parentChain;
    private UrlRewriter urlRewriter;

    public RuleChain(UrlRewriter urlRewriter, String originalUrl, FilterChain parentChain) {
        this.finalToUrl = originalUrl;
        this.urlRewriter = urlRewriter;
        this.rules = urlRewriter.getConf().getRules();
        this.parentChain = parentChain;
    }

    private void doRuleProcessing(HttpServletRequest hsRequest, HttpServletResponse hsResponse)
            throws IOException, ServletException, InvocationTargetException {
        // return to next level up and contniue to process rules
        int currentIdx = ruleIdxToRun++;
        final Rule rule = (Rule) rules.get(currentIdx);
        final RewrittenUrl rewrittenUrl = rule.matches(finalToUrl, hsRequest, hsResponse, this);

        // if this is a filter don't process any more rules, only process them via doFilter
        if (rule.isFilter()) {
            dontProcessAnyMoreRules();
        }
        if (rewrittenUrl != null) {
            log.trace("got a rewritten url");
            // if doFilter was used and final rewritten url is null
            finalRewrittenRequest = rewrittenUrl;
            finalToUrl = rewrittenUrl.getTarget();
            if (rule.isLast()) {
                log.debug("rule is last");
                // there can be no more matches on this request
                dontProcessAnyMoreRules();
            }
        }
        // rule terminated and doFilter wasn't called
        // if doFilter wasn't called then either execute the returning object or assume run has handled it
    }

    private void dontProcessAnyMoreRules() {
        ruleIdxToRun = rules.size();
    }

    public RewrittenUrl getFinalRewrittenRequest() {
        return finalRewrittenRequest;
    }

    public boolean isResponseHandled() {
        return responseHandled;
    }

    public void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        try {
            process(request, response);
            handleRewrite(request, response);
        } catch (InvocationTargetException e) {
            handleExcep(request, response, e);
        }
    }

    private void handleExcep(ServletRequest request, ServletResponse response, InvocationTargetException e)
            throws IOException, ServletException {
        dontProcessAnyMoreRules();
        finalRewrittenRequest = urlRewriter.handleInvocationTargetException((HttpServletRequest) request,
                (HttpServletResponse) response, e);
        handleRewrite(request, response);
    }

    public void process(ServletRequest request, ServletResponse response)
            throws IOException, ServletException, InvocationTargetException {
        while (ruleIdxToRun < rules.size()) {
            doRuleProcessing((HttpServletRequest) request, (HttpServletResponse) response);
        }
    }

    public void doRules(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        try {
            process(request, response);
            handleRewrite(request, response);
        } catch (InvocationTargetException e) {
            handleExcep(request, response, e);

        } catch (ServletException e) {
            if ( e.getCause() instanceof InvocationTargetException ) {
                handleExcep(request, response, (InvocationTargetException) e.getCause());
            }   else {
                throw e;
            }
        }
    }

    private void handleRewrite(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (rewriteHandled) return;
        rewriteHandled = true;
        // wrap the request if necessary to allow overriding of request parameters and method
        if ( response instanceof UrlRewriteWrappedResponse && request instanceof HttpServletRequest) {
            HashMap overiddenRequestParameters = ((UrlRewriteWrappedResponse) response).getOverridenRequestParameters();
            String overiddenMethod = ((UrlRewriteWrappedResponse) response).getOverridenMethod();
            if ( overiddenRequestParameters != null || overiddenMethod != null) {
                request = new UrlRewriteWrappedRequest((HttpServletRequest) request, overiddenRequestParameters, overiddenMethod);
            }
        }
        if (finalRewrittenRequest != null) {
            responseHandled = true;
            requestRewritten = finalRewrittenRequest.doRewrite((HttpServletRequest) request,
                    (HttpServletResponse) response, parentChain);
        }
        if (! requestRewritten) {
            responseHandled = true;
            parentChain.doFilter(request, response);
        }
    }

}
