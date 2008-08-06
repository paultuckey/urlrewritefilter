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

import javax.servlet.ServletContext;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.utils.Log;


public class RuleExecutionOutput {

    private static Log log = Log.getLog(RuleExecutionOutput.class);

    private String replacedUrl;
    private ServletContext replacedUrlContext = null;
    private boolean ruleMatched = false;
    private boolean stopFilterMatch = false;
    private boolean noSubstitution = false;
    private RewriteMatch rewriteMatch;

    /**
     * Will perform the action defined by the rule ie, redirect or passthrough.
     *
     * @param ruleExecutionOutput
     */
    public static RewrittenUrl getRewritenUrl(short toType, boolean encodeToUrl, RuleExecutionOutput ruleExecutionOutput) {

        NormalRewrittenUrl rewrittenRequest = new NormalRewrittenUrl(ruleExecutionOutput);
        String toUrl = ruleExecutionOutput.getReplacedUrl();

        if (ruleExecutionOutput.isNoSubstitution()) {
            if (log.isDebugEnabled()) {
                log.debug("needs no substitution");
            }
        } else if (toType == NormalRule.TO_TYPE_REDIRECT) {
            if (log.isDebugEnabled()) {
                log.debug("needs to be redirected to " + toUrl);
            }
            rewrittenRequest.setRedirect(true);

        } else if (toType == NormalRule.TO_TYPE_PERMANENT_REDIRECT) {
            if (log.isDebugEnabled()) {
                log.debug("needs to be permanentely redirected to " + toUrl);
            }
            rewrittenRequest.setPermanentRedirect(true);

        } else if (toType == NormalRule.TO_TYPE_TEMPORARY_REDIRECT) {
            if (log.isDebugEnabled()) {
                log.debug("needs to be temporarily redirected to " + toUrl);
            }
            rewrittenRequest.setTemporaryRedirect(true);

        } else if (toType == NormalRule.TO_TYPE_PRE_INCLUDE) {
            if (log.isDebugEnabled()) {
                log.debug(toUrl + " needs to be pre included");
            }
            rewrittenRequest.setPreInclude(true);

        } else if (toType == NormalRule.TO_TYPE_POST_INCLUDE) {
            if (log.isDebugEnabled()) {
                log.debug(toUrl + " needs to be post included");
            }
            rewrittenRequest.setPostInclude(true);

        } else if (toType == NormalRule.TO_TYPE_FORWARD) {

            // pass the request to the "to" url
            if (log.isDebugEnabled()) {
                log.debug("needs to be forwarded to " + toUrl);
            }
            rewrittenRequest.setForward(true);
        } else if (toType == NormalRule.TO_TYPE_PROXY) {
            // pass the request to the "to" url
            if (log.isDebugEnabled()) {
                log.debug("needs to be proxied from " + toUrl);
            }
            rewrittenRequest.setProxy(true);
        }

        if (encodeToUrl) {
            rewrittenRequest.setEncode(true);
        } else {
            rewrittenRequest.setEncode(false);
        }

        return rewrittenRequest;
    }

    public RuleExecutionOutput(String replacedUrl, boolean ruleMatched, RewriteMatch lastRunMatch) {
        this.replacedUrl = replacedUrl;
        this.ruleMatched = ruleMatched;
        this.rewriteMatch = lastRunMatch;
    }

    public String getReplacedUrl() {
        return replacedUrl;
    }

    public boolean isRuleMatched() {
        return ruleMatched;
    }

    public boolean isStopFilterMatch() {
        return stopFilterMatch;
    }

    public void setStopFilterMatch(boolean stopFilterMatch) {
        this.stopFilterMatch = stopFilterMatch;
    }

    public void setReplacedUrl(String replacedUrl) {
        this.replacedUrl = replacedUrl;
    }

    public RewriteMatch getRewriteMatch() {
        return rewriteMatch;
    }

    /**
     * @return the replacedUrlContext
     */
    public ServletContext getReplacedUrlContext() {
        return replacedUrlContext;
    }

    /**
     * @param replacedUrlContext the replacedUrlContext to set
     */
    public void setReplacedUrlContext(ServletContext replacedUrlContext) {
        this.replacedUrlContext = replacedUrlContext;
    }

    public boolean isNoSubstitution() {
    	return noSubstitution;
    }

    public void setNoSubstitution(boolean noSubstitution) {
    	this.noSubstitution = noSubstitution;
    }

}
