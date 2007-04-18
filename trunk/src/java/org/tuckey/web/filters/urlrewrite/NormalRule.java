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

    private boolean encodeToUrl = false;

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

}
