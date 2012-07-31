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
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;


/**
 * Defines a rule that can be run against an incoming request.
 * 20040304 - Thanks to Scott Askew for help with concurrency issue with Perl5Compiler and Perl5Matcher.
 *
 * @author Paul Tuckey
 * @version $Revision: 36 $ $Date: 2006-09-19 18:32:39 +1200 (Tue, 19 Sep 2006) $
 */
public class NormalRule extends RuleBase implements Rule {

    private static Log log = Log.getLog(NormalRule.class);

    public short toType = TO_TYPE_FORWARD;  // default to passthrough/forward

    public static final short TO_TYPE_REDIRECT = 0;
    public static final short TO_TYPE_FORWARD = 1;
    public static final short TO_TYPE_PERMANENT_REDIRECT = 2;
    public static final short TO_TYPE_TEMPORARY_REDIRECT = 3;
    public static final short TO_TYPE_PRE_INCLUDE = 4;
    public static final short TO_TYPE_POST_INCLUDE = 5;
    public static final short TO_TYPE_PROXY = 6;

    private boolean encodeToUrl = false;
    private boolean queryStringAppend = false;
    private String toContextStr = null;
    private ServletContext toServletContext = null;

    /**
     * Constructor.
     */
    public NormalRule() {
        // empty
    }

    /**
     * Will run the rule against the uri and perform action required will return false is not matched
     * otherwise true.
     *
     * @param url
     * @param hsRequest
     * @return String of the rewritten url or the same as the url passed in if no match was made
     */
    public RewrittenUrl matches(final String url, final HttpServletRequest hsRequest,
                                final HttpServletResponse hsResponse, RuleChain chain)
            throws IOException, ServletException, InvocationTargetException {
        RuleExecutionOutput ruleExecutionOutput = super.matchesBase(url, hsRequest, hsResponse, chain);
        if (ruleExecutionOutput == null || !ruleExecutionOutput.isRuleMatched()) {
            // no match, or run/set only match
            return null;
        }
        if ( queryStringAppend && hsRequest.getQueryString() != null && hsRequest.getQueryString().length() > 0) {
            String target = ruleExecutionOutput.getReplacedUrl();
            if (target.contains("?")) {
                ruleExecutionOutput.setReplacedUrl(target + "&" + hsRequest.getQueryString());
            } else {
                ruleExecutionOutput.setReplacedUrl(target + "?" + hsRequest.getQueryString());
            }
        }
        if ( toServletContext != null ) ruleExecutionOutput.setReplacedUrlContext(toServletContext);
        return RuleExecutionOutput.getRewritenUrl(toType, encodeToUrl, ruleExecutionOutput);
    }

    public RewrittenUrl matches(final String url, final HttpServletRequest hsRequest,
                                final HttpServletResponse hsResponse)
            throws IOException, ServletException, InvocationTargetException {
        return matches(url, hsRequest, hsResponse, null);
    }


    /**
     * Will initialise the rule.
     *
     * @return true on success
     */
    public boolean initialise(ServletContext context) {
        boolean ok = super.initialise(context);
        // check all the conditions
        initialised = true;
        if (!ok) {
            log.debug("failed to load rule");
        } else {
            log.debug("loaded rule " + getDisplayName() + " (" + from + ", " + to + " " + toType + ")");
        }

        if ( !StringUtils.isBlank(toContextStr)) {
            log.debug("looking for context " + toContextStr);
            if ( context == null) {
                addError("unable to look for context as current context null");
            }   else {
                toServletContext = context.getContext("/" + toContextStr);
                if ( toServletContext == null ) {
                    addError("could not get servlet context " + toContextStr);
                }   else {
                    log.debug("got context ok");
                }
            }
        }

        if (errors.size() > 0) {
            ok = false;
        }
        valid = ok;
        return ok;
    }


    /**
     * Redirect or passthrough, passthrough is the default.
     *
     * @param toTypeStr to type string
     */
    public void setToType(final String toTypeStr) {
        if ("redirect".equals(toTypeStr)) {
            toType = TO_TYPE_REDIRECT;
        } else if ("permanent-redirect".equals(toTypeStr)) {
            toType = TO_TYPE_PERMANENT_REDIRECT;
        } else if ("temporary-redirect".equals(toTypeStr)) {
            toType = TO_TYPE_TEMPORARY_REDIRECT;
        } else if ("pre-include".equals(toTypeStr)) {
            toType = TO_TYPE_PRE_INCLUDE;
        } else if ("post-include".equals(toTypeStr)) {
            toType = TO_TYPE_POST_INCLUDE;
        } else if ("forward".equals(toTypeStr) || "passthrough".equals(toTypeStr) ||
                StringUtils.isBlank(toTypeStr)) {
            toType = TO_TYPE_FORWARD;
        } else if ("proxy".equals(toTypeStr)) {
            toType = TO_TYPE_PROXY;
        } else {
            addError("type (" + toTypeStr + ") is not valid");
        }
    }

    /**
     * Will get to type ie redirect or passthrough.
     *
     * @return String
     */
    public String getToType() {
        if (toType == TO_TYPE_REDIRECT) return "redirect";
        if (toType == TO_TYPE_PERMANENT_REDIRECT) return "permanent-redirect";
        if (toType == TO_TYPE_TEMPORARY_REDIRECT) return "temporary-redirect";
        if (toType == TO_TYPE_PRE_INCLUDE) return "pre-include";
        if (toType == TO_TYPE_POST_INCLUDE) return "post-include";
        if (toType == TO_TYPE_PROXY) return "proxy";
        return "forward";
    }

    protected void addError(String s) {
        log.error("Rule " + getDisplayName() + " had error: " + s);
        super.addError(s);
    }

    public String getDisplayName() {
        if (name != null) {
            return name + " (rule " + id + ")";
        }
        return "Rule " + id;
    }

    public String getName() {
        return name;
    }

    public String getFullDisplayName() {
        return getDisplayName() + " (" + from + ", " + to + " " + toType + ")";
    }

    public boolean isEncodeToUrl() {
        return encodeToUrl;
    }

    public void setEncodeToUrl(boolean encodeToUrl) {
        this.encodeToUrl = encodeToUrl;
    }

    public String getToContextStr() {
        return toContextStr;
    }

    public void setToContextStr(String toContextStr) {
        this.toContextStr = toContextStr;
    }

    public ServletContext getToServletContext() {
        return toServletContext;
    }

    public void setQueryStringAppend(String value) {
        queryStringAppend = "true".equalsIgnoreCase(value);
    }
}
