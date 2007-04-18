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
public final class OutboundRule extends RuleBase {

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


