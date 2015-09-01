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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Outputs information about urlrewritefilter.
 * <p/>
 * todo: add ability to trigger reload conf
 *
 * @author Paul Tuckey
 * @version $Revision: 43 $ $Date: 2006-10-31 17:29:59 +1300 (Tue, 31 Oct 2006) $
 */
public class Status {

    private static Log log = Log.getLog(Status.class);

    private StringBuffer buffer = new StringBuffer();

    private Conf conf;
    private UrlRewriteFilter urlRewriteFilter;

    public Status(Conf conf) {
        this.conf = conf;
    }

    public Status(Conf conf, UrlRewriteFilter urlRewriteFilter) {
        this.conf = conf;
        this.urlRewriteFilter = urlRewriteFilter;
    }

    public void displayStatusInContainer(final HttpServletRequest hsRequest) {
        showHeader();
        showRunningInfo();
        showConf();
        showRequestInfo(hsRequest);
        showFooter();
    }

    public void displayStatusOffline() {
        showHeader();
        showConf();
        showFooter();
    }


    private void showRequestInfo(final HttpServletRequest hsRequest) {
        // other info
        println("<h2>Request Debug Info</h2>");

        println("<h4>General</h4>");
        println("<pre>");

        println("method: " + hsRequest.getMethod());
        if (hsRequest.getAuthType() != null) println("auth-type: " + hsRequest.getAuthType());
        if (hsRequest.getCharacterEncoding() != null)
            println("character-encoding: " + hsRequest.getCharacterEncoding());
        println("context-path: " + hsRequest.getContextPath());
        if (hsRequest.getPathInfo() != null) println("path-info: " + hsRequest.getPathInfo());
        if (hsRequest.getPathTranslated() != null) println("path-translated: " + hsRequest.getPathTranslated());
        println("port: " + hsRequest.getServerPort());
        println("protocol: " + hsRequest.getProtocol());
        if (hsRequest.getQueryString() != null) println("query-string: " + hsRequest.getQueryString());
        println("remote-addr: " + hsRequest.getRemoteAddr());
        println("remote-host: " + hsRequest.getRemoteHost());
        if (hsRequest.getRemoteUser() != null) println("remote-user: " + hsRequest.getRemoteUser());
        if (hsRequest.getRequestedSessionId() != null)
            println("requested-session-id: " + hsRequest.getRequestedSessionId());
        println("request-uri: " + hsRequest.getRequestURI());
        println("request-url: " + hsRequest.getRequestURL());
        println("server-name: " + hsRequest.getServerName());
        println("scheme: " + hsRequest.getScheme());

        println("</pre>");

        HttpSession session = hsRequest.getSession(false);
        if (session != null) {
            println("<h4>Session</h4>");
            println("<br />session-isnew: " + session.isNew());
            Enumeration enumer = session.getAttributeNames();
            while (enumer.hasMoreElements()) {
                String name = (String) enumer.nextElement();
                println("<br />session-attribute " + name + ": " + session.getAttribute(name));
            }
        }

        // show headers from request
        println("<h4>Request Headers</h4>");
        println("<pre>");
        final Enumeration headers = hsRequest.getHeaderNames();
        while (headers.hasMoreElements()) {
            final String headerName = (String) headers.nextElement();
            // ignore cookies as they are handled later
            if ("cookie".equals(headerName)) continue;
            println(headerName + ": " + hsRequest.getHeader(headerName));
        }
        println("</pre>");

        final Cookie[] cookies = hsRequest.getCookies();
        if (cookies != null && cookies.length > 0) {
            println("<h4>Cookies</h4>");
            for (int i = 0; i < cookies.length; i++) {
                println("<h5>Cookie " + i + "</h5>");
                final Cookie cookie = cookies[i];
                if (cookie == null) continue;
                println("<pre>");
                println("    name     : " + cookie.getName());
                println("    value    : " + cookie.getValue());
                println("    path     : " + cookie.getPath());
                println("    domain   : " + cookie.getDomain());
                println("    max age  : " + cookie.getMaxAge());
                println("    is secure: " + cookie.getSecure());
                println("    version  : " + cookie.getVersion());
                println("    comment  : " + cookie.getComment());
                println("</pre>");
            }
        }

        // show headers from request
        println("<h4>Time info</h4>");
        println("<pre>");
        Calendar nowCal = Calendar.getInstance();
        println("time: " + nowCal.getTime().getTime());
        println("year: " + nowCal.get(Calendar.YEAR));
        println("month: " + nowCal.get(Calendar.MONTH));
        println("dayofmonth: " + nowCal.get(Calendar.DAY_OF_MONTH));
        println("dayofweek: " + nowCal.get(Calendar.DAY_OF_WEEK));
        println("ampm: " + nowCal.get(Calendar.AM_PM));
        println("hourofday: " + nowCal.get(Calendar.HOUR_OF_DAY));
        println("minute: " + nowCal.get(Calendar.MINUTE));
        println("second: " + nowCal.get(Calendar.SECOND));
        println("millisecond: " + nowCal.get(Calendar.MILLISECOND));
        println("</pre>");

    }

    private void showConf() {
        if (conf == null) return;

        println("<h2>Summary");
        if (conf.isLoadedFromFile()) println(" of " + conf.getFileName());
        println("</h2>");

        if (!conf.isOk()) {
            final List errors = conf.getErrors();
            println("<h4 class=\"err\">Errors During Load of " + conf.getFileName() + "</h4>");
            println("<ul>");
            if (errors.size() > 0) {
                for (int i = 0; i < errors.size(); i++) {
                    final String error = (String) errors.get(i);
                    println("<li class=\"err\">" + error + "</li>");
                }
            }
            displayRuleErrors(conf.getRules());
            displayRuleErrors(conf.getOutboundRules());
            displayCatchErrors(conf.getCatchElems());
            println("</ul>");
        }

        int conditionsCount = 0;
        final List rules = conf.getRules();
        for (int i = 0; i < rules.size(); i++) {
            final Rule rule = (Rule) rules.get(i);
            if (rule instanceof NormalRule) {
                conditionsCount += ((NormalRule) rule).getConditions().size();
            }
        }
        final List outboundRules = conf.getOutboundRules();
        for (int i = 0; i < outboundRules.size(); i++) {
            final OutboundRule rule = (OutboundRule) outboundRules.get(i);
            conditionsCount += rule.getConditions().size();
        }
        println("<p>In total there " +
                (rules.size() == 1 ? "is 1 rule" : "are " + rules.size() + " rules") + ", " +
                (outboundRules.size() == 1 ? "1 outbound rule" : outboundRules.size() + " outbound rules") +
                (conditionsCount > 0 ? " and " : "") +
                (conditionsCount == 1 ? conditionsCount + " condition" : "") +
                (conditionsCount > 1 ? conditionsCount + " conditions" : "") +
                " in the configuration file.</p>");

        showRules(rules);
        showOutboundRules(outboundRules);
        println("<hr />");
    }

    private void showRules(List rules) {
        for (int i = 0; i < rules.size(); i++) {
            final Rule rule = (Rule) rules.get(i);
            if (rule instanceof NormalRule) {
                NormalRule normalRule = (NormalRule) rule;
                println("<h3>" + normalRule.getDisplayName() +
                        (normalRule.isEnabled() ? "" : " **DISABLED**") + "</h3>");
                if (!StringUtils.isBlank(normalRule.getNote())) {
                    println("<dl><dd><p>" + StringUtils.nl2br(normalRule.getNote()) + "</p></dd></dl>");
                }

                print("<p>URL's matching <code>" + normalRule.getFrom() + "</code>");
                if (normalRule.isFilter()) {
                    print(" (filter)");
                }
                if (!StringUtils.isBlank(normalRule.getTo())) {
                    print(" will ");
                    if ("forward".equals(normalRule.getToType()))
                        print("be <code>forwarded</code> to");
                    else if ("include".equals(normalRule.getToType()))
                        print("<code>include</code>");
                    else if ("redirect".equals(normalRule.getToType()))
                        print("be <code>redirected</code> to");
                    else
                        print("<code>" + normalRule.getToType() + "</code> to");
                    print(" <code>" + normalRule.getTo() + "</code>");
                }
                println(".</p>");
                print("<p>This rule and it's conditions will use the <code>" + normalRule.getMatchType() + "</code> matching engine.</p>");
                showConditions(normalRule);
                showSets(normalRule);
                showRuns(normalRule);

                if (!rule.isLast()) {
                    println("<p>Note, other rules will be processed after this rule.</p>");
                }
            }
            if (rule instanceof ClassRule) {
                ClassRule classRule = (ClassRule) rule;
                println("<h3>" + classRule.getDisplayName() +
                        (classRule.isEnabled() ? "" : " **DISABLED**") + "</h3>");
            }
            println();
            println();
        }
    }

    private void showOutboundRules(List outboundRules) {
        for (int i = 0; i < outboundRules.size(); i++) {
            final OutboundRule rule = (OutboundRule) outboundRules.get(i);
            println("<h3>" + rule.getDisplayName() +
                    (rule.isEnabled() ? "" : " **DISABLED**") + "</h3>");
            if (!StringUtils.isBlank(rule.getNote())) {
                println("<dl><dd><p>" + StringUtils.nl2br(rule.getNote()) + "</p></dd></dl>");
            }
            print("<p>Outbound URL's matching <code>" + rule.getFrom() + "</code>");
            if (!StringUtils.isBlank(rule.getTo())) {
                print(" will be rewritten to <code>" + rule.getTo() + "</code>");
            }
            if (!rule.isEncodeFirst()) {
                print(", after <code>response.encodeURL()</code> has been called");
            }
            if (!rule.isEncodeToUrl()) {
                print(", <code>response.encodeURL()</code> will not be called");
            }
            println(".</p>");

            showConditions(rule);
            showSets(rule);
            showRuns(rule);

            if (!rule.isLast()) {
                println("<p>Note, other outbound rules will be processed after this rule.</p>");
            }
            println();
            println();
        }
    }

    private void showHeader() {
        SimpleDateFormat s = new SimpleDateFormat();
        println("<!DOCTYPE html>");
        println("<html lang=\"en\">");
        println("<head>");
        if ( conf == null ) {
            println("<title>UrlRewriteFilter configuration overview</title>");
        } else {
            println("<title>UrlRewriteFilter configuration overview for " + conf.getFileName() + "</title>");
        }
        println("<style type=\"text/css\">");
        InputStream is = Status.class.getResourceAsStream("doc/doc.css");
        if (is == null) {
            log.warn("unable to load style sheet");
        } else {
            try {
                for (int i = is.read(); i != -1; i = is.read()) {
                    buffer.append((char) i);
                }
            } catch (IOException e) {
                // don't care about this too much
            }
        }
        println("</style>");
        println("<body>");
        println("<h1><a href=\"http://www.tuckey.org/urlrewrite/\">UrlRewriteFilter</a> " +
                UrlRewriteFilter.getFullVersionString() + " configuration overview " +
                "(generated " + s.format(new Date()) + ")</h1>");
        println("<hr />");
    }

    private void showRunningInfo() {
        println("<h2>Running Status</h2>");
        if (conf == null) {
            println("<h3 class=\"err\">ERROR: UrlRewriteFilter failed to load config, check server log</h3>");
        }   else if (!conf.isOk()) {
            println("<h3 class=\"err\">ERROR: UrlRewriteFilter NOT ACTIVE</h3>");
        }
        println("<p>Conf");
        if (conf == null ) println(" <em>empty</em>.</p>");
        if (conf != null && conf.isLoadedFromFile()) println("file <code>" + conf.getFileName() + "</code>");
        if (conf != null ) println("loaded <em>" + conf.getLoadedDate() + "</em>.</p>");
        if (urlRewriteFilter != null) {
            if (urlRewriteFilter.isConfReloadCheckEnabled()) {
                Date nextReloadCheckDate = new Date(urlRewriteFilter.getConfReloadLastCheck().getTime() +
                        (urlRewriteFilter.getConfReloadCheckInterval() * 1000L));
                println("<p>Conf file reload check <em>enabled</em>, last modified will be checked every <em>" +
                        urlRewriteFilter.getConfReloadCheckInterval() + "s</em>, last checked <em>" +
                        urlRewriteFilter.getConfReloadLastCheck() + "</em>, next check at <em>" +
                        nextReloadCheckDate + "</em> in <em>" +
                        Math.round((nextReloadCheckDate.getTime() - System.currentTimeMillis()) / 1000d) + "s</em>.");
            } else {
                println("Conf file reload check <em>disabled</em>");
            }
            println("<p>Status path <code>" + urlRewriteFilter.getStatusPath() + "</code>.</p>");
        }
    }

    private void displayRuleErrors(final List rules) {
        for (int i = 0; i < rules.size(); i++) {
            Object ruleObj = rules.get(i);
            if (ruleObj instanceof Rule) {
                final Rule rule = (Rule) rules.get(i);
                if (rule.isValid()) continue;
                println("<li class=\"err\">Error in " + rule.getDisplayName());
                println("<ul>");
                List ruleErrors = rule.getErrors();
                for (int j = 0; j < ruleErrors.size(); j++) {
                    println("<li class=\"err\">" + ruleErrors.get(j) + "</li>");
                }
                if (rule instanceof NormalRule) {
                    NormalRule normalRule = (NormalRule) rule;
                    List conditions = normalRule.getConditions();
                    List sets = normalRule.getSetAttributes();
                    List runs = normalRule.getRuns();
                    displayRuleCondSetRun(conditions, sets, runs);
                }
                println("</ul></li>");
            }
            if (ruleObj instanceof OutboundRule) {
                final OutboundRule outboundRule = (OutboundRule) rules.get(i);
                if (outboundRule.isValid()) continue;
                println("<li class=\"err\">Error in " + outboundRule.getDisplayName());
                println("<ul>");
                List ruleErrors = outboundRule.getErrors();
                for (int j = 0; j < ruleErrors.size(); j++) {
                    println("<li class=\"err\">" + ruleErrors.get(j) + "</li>");
                }
                List conditions = outboundRule.getConditions();
                List sets = outboundRule.getSetAttributes();
                List runs = outboundRule.getRuns();
                displayRuleCondSetRun(conditions, sets, runs);
                println("</ul></li>");
            }
        }
    }

    private void displayRuleCondSetRun(List conditions, List sets, List runs) {
        for (int j = 0; j < conditions.size(); j++) {
            Condition condition = (Condition) conditions.get(j);
            if (condition.getError() == null) continue;
            println("<li class=\"err\">" + condition.getDisplayName() + " " + condition.getError() + "</li>");
        }
        for (int j = 0; j < sets.size(); j++) {
            SetAttribute setAttribute = (SetAttribute) sets.get(j);
            if (setAttribute.getError() == null) continue;
            println("<li class=\"err\">" + setAttribute.getDisplayName() + " " + setAttribute.getError() + "</li>");
        }
        for (int j = 0; j < runs.size(); j++) {
            Run run = (Run) runs.get(j);
            if (run.getError() == null) continue;
            println("<li class=\"err\">" + run.getDisplayName() + " " + run.getError() + "</li>");
        }
    }

    private void displayCatchErrors(final List catchElems) {
        for (int i = 0; i < catchElems.size(); i++) {
            final CatchElem catchElem = (CatchElem) catchElems.get(i);
            if (catchElem.isValid()) continue;
            println("<li class=\"err\">Error in catch for " + catchElem.getClass() + "</li>");
            println("<ul>");
            List runs = catchElem.getRuns();
            for (int j = 0; j < runs.size(); j++) {
                Run run = (Run) runs.get(j);
                if (run.getError() == null) continue;
                println("<li class=\"err\">" + run.getDisplayName() + " " + run.getError() + "</li>");
            }
            println("</ul></li>");
        }
    }

    private void showSets(final RuleBase rule) {
        if (rule.getSetAttributes().size() == 0) return;
        List setAttributes = rule.getSetAttributes();
        println("<p>This rule will set:</p>" +
                "<ol>");
        for (int j = 0; j < setAttributes.size(); j++) {
            SetAttribute setAttribute = (SetAttribute) setAttributes.get(j);
            println("<li>");
            if ("response-header".equals(setAttribute.getType())) {
                println("The <code>" + setAttribute.getName() + "</code> HTTP response header " +
                        "to <code>" + setAttribute.getValue() + "</code>");

            } else if ("request".equals(setAttribute.getType()) ||
                    "session".equals(setAttribute.getType())) {
                println("An attribute on the <code>" + setAttribute.getType() + "</code> object " +
                        "called <code>" + setAttribute.getName() + "</code> " +
                        "to the value " +
                        "<code>" + setAttribute.getValue() + "</code>");
            } else if ("cookie".equals(setAttribute.getType())) {
                println("A cookie " +
                        "called <code>" + setAttribute.getName() + "</code> " +
                        " to the value " +
                        "<code>" + setAttribute.getValue() + "</code>");
            } else if ("locale".equals(setAttribute.getType())) {
                println("locale to " +
                        "<code>" + setAttribute.getValue() + "</code>");
            } else if ("status".equals(setAttribute.getType())) {
                println("status to " +
                        "<code>" + setAttribute.getValue() + "</code>");
            } else if ("content-type".equals(setAttribute.getType())) {
                println("content-type to " +
                        "<code>" + setAttribute.getValue() + "</code>");
            } else if ("charset".equals(setAttribute.getType())) {
                println("charset to " +
                        "<code>" + setAttribute.getValue() + "</code>");
            }
            println("</li>");

        }
        println("</ol>");
    }

    private void showRuns(RuleBase rule) {
        List runs = rule.getRuns();
        if (runs.size() == 0) return;

        println("<p>This rule will run:</p>" +
                "<ol>");
        for (int j = 0; j < runs.size(); j++) {
            Run run = (Run) runs.get(j);
            println("<li>");
            println(" <code>" + run.getMethodSignature() + "</code> on an instance " +
                    "of " + "<code>" + run.getClassStr() + "</code>");
            if (run.isNewEachTime()) {
                println(" (a new instance will be created for each rule match)");
            }
            println("</li>");
        }
        println("</ol>");
        println("<small>Note, if <code>init(ServletConfig)</code> or <code>destroy()</code> is found on the above " +
                "object" + (runs.size() > 1 ? "s" : "") + " they will be run at when creating or destroying an instance.</small>");
    }

    private void showConditions(RuleBase rule) {
        List conditions = rule.getConditions();
        if (conditions.size() == 0) return;

        println("<p>Given that the following condition" +
                (conditions.size() == 1 ? " is" : "s are") + " met.</p>" +
                "<ol>");
        for (int j = 0; j < conditions.size(); j++) {
            Condition condition = (Condition) conditions.get(j);
            println("<li>");
            if ("header".equals(condition.getType())) {
                println("The <code>" + condition.getName() + "</code> HTTP header " +
                        ("notequal".equals(condition.getOperator()) ? "does NOT match" : "matches") + " the value " +
                        "<code>" + condition.getValue() + "</code>");
            } else {
                println("<code>" + condition.getType() + "</code> " +
                        (condition.getName() == null ? "" : "<code>" + condition.getName() + "</code> ") +
                        "is <code>" +
                        ("greater".equals(condition.getOperator()) ? "greater than" : "") +
                        ("less".equals(condition.getOperator()) ? "less than" : "") +
                        ("equal".equals(condition.getOperator()) ? "equal to" : "") +
                        ("notequal".equals(condition.getOperator()) ? "NOT equal to" : "") +
                        ("greaterorequal".equals(condition.getOperator()) ? "is greater than or equal to" : "") +
                        ("lessorequal".equals(condition.getOperator()) ? "is less than or equal to" : "") +
                        "</code> the value <code>" +
                        (StringUtils.isBlank(condition.getValue()) ? condition.getName() : condition.getValue()) + "</code>");
            }
            if (j < conditions.size() - 1) {
                println("<code>" + condition.getNext() + "</code>");
            }
            println("</li>");

        }
        println("</ol>");
    }

    private void showFooter() {
        println("<br /><br /><br />");
        println("</body>");
        println("</html>");
    }

    private void println() {
        buffer.append("\n");
    }

    private void print(String s) {
        buffer.append(s);
    }

    private void println(String s) {
        buffer.append(s);
        println();
    }

    public StringBuffer getBuffer() {
        return buffer;
    }

}



