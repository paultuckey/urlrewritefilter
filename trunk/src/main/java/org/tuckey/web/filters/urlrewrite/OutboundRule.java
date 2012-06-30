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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Defines an outbound rule that can be run against a link in a page.
 *
 * @author Paul Tuckey
 * @version $Revision: 36 $ $Date: 2006-09-19 18:32:39 +1200 (Tue, 19 Sep 2006) $
 */
public class OutboundRule extends RuleBase {

    private static final Log log = Log.getLog(OutboundRule.class);

    private boolean encodeFirst;
    private boolean encodeToUrl = true;

    /**
     * Constructor.
     */
    public OutboundRule() {
        super();
        // empty
    }

    /**
     * Execute this outbound rule.
     */
    public RewrittenOutboundUrl execute(String url, HttpServletRequest hsRequest, HttpServletResponse hsResponse)
            throws InvocationTargetException {
        RuleExecutionOutput ruleRuleExecutionOutput;

        // if we are running a "run" it might result in an exception and we have to handle it
        // unfortunately the servlet spec dow no allow throwing exceptions from encodeURL
        // so we have to throw a runtimeException
        try {
            ruleRuleExecutionOutput = super.matchesBase(url, hsRequest, hsResponse, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }

        if (ruleRuleExecutionOutput == null || !ruleRuleExecutionOutput.isRuleMatched()) {
            return null; // no match
        }
        return new RewrittenOutboundUrl(ruleRuleExecutionOutput.getReplacedUrl(), this.encodeToUrl);
    }

    /**
     * Will initialise the outbound rule.
     *
     * @return true on success
     */
    public boolean initialise(ServletContext servletContext) {
        boolean ok = super.initialise(servletContext);
        // check all the conditions

        if (ok) {
            String displayName = getDisplayName();
            log.debug("loaded outbound rule " + displayName + " (" + from + ", " + to + ')');
        } else {
            log.debug("failed to load outbound rule");
        }
        if (errors.size() > 0) {
            ok = false;
        }
        valid = ok;
        return ok;
    }

    protected void addError(String s) {
        String displayName = getDisplayName();
        log.error("Outbound Rule " + displayName + " had error: " + s);
        super.addError(s);
    }

    public String getDisplayName() {
        if (name != null) {
            return name + " (outbound rule " + id + ')';
        }
        return "Outbound Rule " + id;
    }

    public boolean isEncodeFirst() {
        return encodeFirst;
    }

    public boolean isEncodeToUrl() {
        return encodeToUrl;
    }

    public void setEncodeFirst(boolean encodeFirst) {
        this.encodeFirst = encodeFirst;
    }

    public void setEncodeToUrl(boolean encodeToUrl) {
        this.encodeToUrl = encodeToUrl;
    }
}


