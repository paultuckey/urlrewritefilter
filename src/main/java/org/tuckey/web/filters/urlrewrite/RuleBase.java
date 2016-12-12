/*
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

import com.google.common.base.Strings;
import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.substitution.*;
import org.tuckey.web.filters.urlrewrite.utils.*;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines a rule that can be run against an incoming request.
 *
 * @author Paul Tuckey
 * @version $Revision: 36 $ $Date: 2006-09-19 18:32:39 +1200 (Tue, 19 Sep 2006) $
 */
public class RuleBase implements Runnable {

    private static Log log = Log.getLog(RuleBase.class);

    private static final String DEFAULT_RULE_FROM = "^(.*)$";

    protected int id;

    private boolean enabled = true;

    private boolean fromCaseSensitive;
    protected boolean initialised;
    protected boolean valid;

    protected String name;
    private String note;
    protected String from;
    protected String to;
    private boolean toEmpty;
    private String matchType;
    private boolean last = false;

    private int conditionIdCounter;
    private int runIdCounter;

    private StringMatchingPattern pattern;
    protected final List<String> errors = new ArrayList<>(5);
    private final List<Condition> conditions = new ArrayList<>(5);
    private final List<Run> runs = new ArrayList<>(2);
    protected final List<SetAttribute> setAttributes = new ArrayList<>(2);
    private boolean stopFilterChainOnMatch = false;
    private boolean noSubstitution = false;

    private boolean toContainsVariable = false;
    private boolean toContainsBackReference = false;
    private boolean toContainsFunction = false;

    public static final String MATCH_TYPE_WILDCARD = "wildcard";
    public static final String DEFAULT_MATCH_TYPE = "regex";

    private boolean filter = false;
    private ServletContext servletContext;

    /**
     * Will run the rule against the uri and perform action required will return false is not matched
     * otherwise true.
     *
     * @return String of the rewritten url or the same as the url passed in if no match was made
     */
    protected RuleExecutionOutput matchesBase(String url, final HttpServletRequest hsRequest,
                                              final HttpServletResponse hsResponse, RuleChain chain)
            throws IOException, ServletException, InvocationTargetException {
        // make sure all the conditions match
        if (log.isDebugEnabled()) {
            String displayName = getDisplayName();
            log.debug(displayName + " run called with " + url);
        }
        if (!initialised) {
            log.debug("not initialised, skipping");
            return null;
        }
        if (!valid) {
            log.debug("not valid, skipping");
            return null;
        }
        if (!enabled) {
            log.debug("not enabled, skipping");
            return null;
        }
        if (url == null) {
            log.debug("url is null (maybe because of a previous match), skipping");
            return null;
        }

        StringMatchingMatcher matcher = pattern.matcher(url);
        boolean performToReplacement = false;
        if (toEmpty || stopFilterChainOnMatch) {
            // to is empty this must be an attempt to "set" and/or "run"
            if (!matcher.find()) {
                if (log.isTraceEnabled()) {
                    log.trace("no match on \"from\" (to is empty)");
                }
                return null;
            }
        } else {
            if (!matcher.find()) {
                if (log.isTraceEnabled()) {
                    log.trace("no match on \"from\" for " + from + " and " + url);
                }
                return null;
            }
            if (!toEmpty && !noSubstitution) {
                performToReplacement = true;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("matched \"from\"");
        }

        int conditionsSize = conditions.size();
        ConditionMatch lastConditionMatch = null;
        if (conditionsSize > 0) {
            boolean processNextOr = false;
            boolean currentResult = true;
            for (Object condition1 : conditions) {
                final Condition condition = (Condition) condition1;
                ConditionMatch conditionMatch = condition.getConditionMatch(hsRequest);
                if (conditionMatch != null) {
                    lastConditionMatch = conditionMatch;
                }
                boolean conditionMatches = conditionMatch != null;
                if (processNextOr) {
                    currentResult |= conditionMatches;
                } else {
                    // must be and
                    currentResult &= conditionMatches;
                }
                processNextOr = condition.isProcessNextOr();
            }
            if (!currentResult) {
                log.debug("conditions do not match");
                return null;
            } else {
                log.debug("conditions match");
            }
        }

        // set a req attrib in case people want to use it
        hsRequest.setAttribute("org.tuckey.web.filters.urlrewrite.RuleMatched", Boolean.TRUE);

        // make sure the setAttributes are handled
        int setAttributesSize = setAttributes.size();
        if (setAttributesSize > 0) {
            log.trace("setting attributes");
            for (Object setAttribute1 : setAttributes) {
                SetAttribute setAttribute = (SetAttribute) setAttribute1;
                setAttribute.execute(lastConditionMatch, matcher, hsRequest, hsResponse);
            }
        }

        // make sure the runs are handled
        int runsSize = runs.size();
        RewriteMatch lastRunMatch = null;
        if (runsSize > 0) {
            log.trace("performing runs");
            for (Run run : runs) {
                lastRunMatch = run.execute(hsRequest, hsResponse, matcher, lastConditionMatch, chain);
            }
        }

        String replacedTo = null;
        if (performToReplacement && to != null) {
            SubstitutionContext substitutionContext = new SubstitutionContext(hsRequest, matcher, lastConditionMatch, to);
            SubstitutionFilterChain substitutionFilter = ChainedSubstitutionFilters.getDefaultSubstitutionChain(true, toContainsFunction, toContainsVariable, toContainsBackReference, servletContext);
            replacedTo = substitutionFilter.substitute(url, substitutionContext);
        }

        RuleExecutionOutput ruleExecutionOutput = new RuleExecutionOutput(replacedTo, true, lastRunMatch);

        // check for empty to element (valid when only set's)
        if (toEmpty) {
            log.debug("'to' is empty, no rewrite, only 'set' and or 'run'");
            return null;
        }

        // Check for "no substitution" (-)
        if (noSubstitution) {
            log.debug("'to' is '-', no substitution, passing through URL");
            ruleExecutionOutput.setNoSubstitution(true);
            ruleExecutionOutput.setReplacedUrl(url);
        }

        // when match found but need to stop filter chain
        if (stopFilterChainOnMatch) {
            ruleExecutionOutput.setStopFilterMatch(true);
            ruleExecutionOutput.setReplacedUrl(null);
        }
        // note, the rewritten URL is unchanged if there was no <to> element.
        return ruleExecutionOutput;
    }


    public String getDisplayName() {
        return null;
    }


    /**
     * Will initialise the rule.
     *
     * @return true on success
     */
    public boolean initialise(ServletContext context) {
        this.servletContext = context;
        // check all the conditions
        initialised = true;
        boolean ok = true;
        for (final Condition condition : conditions) {
            condition.setRule(this);
            if (!condition.initialise()) {
                ok = false;
            }
        }
        for (final Run run : runs) {
            if (!run.initialise(context)) {
                ok = false;
            }
            if (run.isFilter()) {
                log.debug("rule is a filtering rule");
                filter = true;
            }
        }
        for (final SetAttribute setAttribute : setAttributes) {
            if (!setAttribute.initialise()) {
                ok = false;
            }
        }
        // make sure default set for matchType
        if (!isMatchTypeWildcard()) {
            matchType = DEFAULT_MATCH_TYPE;
        }

        // compile the from regexp
        if (StringUtils.isBlank(from)) {
            log.debug("rule's from is blank, setting to ", DEFAULT_RULE_FROM);
            from = DEFAULT_RULE_FROM;
        }

        try {
            if (isMatchTypeWildcard()) {
                log.debug("rule match type is wildcard");
                pattern = new WildcardPattern(from);

            } else {
                // default is regexp
                pattern = new RegexPattern(from, fromCaseSensitive);
            }

        } catch (StringMatchingPatternSyntaxException e) {
            addError("from (" + from + ") is an invalid expression - " + e.getMessage());
        }

        // set the substitution
        if (StringUtils.isBlank(to) && setAttributes.size() == 0 && runs.size() == 0) {
            addError("to is not valid because it is blank (it is allowed to be blank when there is a 'set' specified)");
        } else if ("null".equalsIgnoreCase(to)) {
            stopFilterChainOnMatch = true;
        } else if ("-".equals(to)) {
            noSubstitution = true;
        } else if (StringUtils.isBlank(to)) {
            toEmpty = true;
        } else if (!StringUtils.isBlank(to)) {
            // check for back refs
            if (BackReferenceReplacer.containsBackRef(to)) {
                toContainsBackReference = true;
            }
            // look for vars
            if (VariableReplacer.containsVariable(to)) {
                toContainsVariable = true;
            }
            // look for functions
            if (FunctionReplacer.containsFunction(to)) {
                toContainsFunction = true;
            }
        }

        if (ok) {
            log.debug("loaded rule ", getFullDisplayName());
        } else {
            log.debug("failed to load rule");
        }
        if (errors.size() > 0) {
            ok = false;
        }
        valid = ok;
        return ok;
    }

    public boolean isMatchTypeWildcard() {
        return MATCH_TYPE_WILDCARD.equalsIgnoreCase(matchType);
    }

    public boolean isToContainsBackReference() {
        return toContainsBackReference;
    }

    public boolean isToContainsVariable() {
        return toContainsVariable;
    }

    public boolean isToContainsFunction() {
        return toContainsFunction;
    }

    public String getFullDisplayName() {
        return null;
    }

    protected void addError(String s) {
        errors.add(s);
        log.error(s);
    }

    /**
     * Destroy the rule gracefully.
     */
    public void destroy() {
        for (final Run run : runs) {
            run.destroy();
        }
    }

    /**
     * Will get the contents of the from element.
     *
     * @return the contents of the from element
     */
    public String getFrom() {
        return from;
    }

    /**
     * Will set from, usually called by Digester.
     *
     * @param from the url to match from
     */
    public void setFrom(final String from) {
        if (!Strings.isNullOrEmpty(from)) {
            this.from = RewriteUtils.uriEncodeParts(from);
        }
    }

    /**
     * Will set the to, usually called by Digester.
     *
     * @param to url for redirecting/passing through to
     */
    public void setTo(final String to) {
        if (!StringUtils.isBlank(to)) {
            this.to = to;
        }
    }

    /**
     * Set to type. note, it will default to false.
     *
     * @param lastStr true or false
     */
    public void setToLast(final String lastStr) {
        last = "true".equalsIgnoreCase(lastStr);
    }

    /**
     * Is this rule last?.
     *
     * @return boolean
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Get to.
     *
     * @return String
     */
    public String getTo() {
        return to;
    }

    /**
     * Will get the rule's id.
     *
     * @return int
     */
    public int getId() {
        return id;
    }


    /**
     * Will get the list of errors.
     *
     * @return the list of errors
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * Will add the condition to the List.
     *
     * @param condition The Condition object to add
     */
    public void addCondition(final Condition condition) {
        conditions.add(condition);
        condition.setId(conditionIdCounter++);
    }

    /**
     * Will add the run to the List.
     *
     * @param run The Run object to add
     */
    public void addRun(final Run run) {
        runs.add(run);
        run.setId(runIdCounter++);
    }

    /**
     * Will add the SetAttribute to the List.
     *
     * @param setAttribute The SetAttribute object to add
     */
    public void addSetAttribute(final SetAttribute setAttribute) {
        setAttributes.add(setAttribute);
    }

    public List<SetAttribute> getSetAttributes() {
        return setAttributes;
    }

    /**
     * Will get the List of conditions.
     *
     * @return the List of Condition objects
     */
    public List<Condition> getConditions() {
        return conditions;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isFromCaseSensitive() {
        return fromCaseSensitive;
    }

    public void setFromCaseSensitive(boolean fromCaseSensitive) {
        this.fromCaseSensitive = fromCaseSensitive;
    }

    public List<Run> getRuns() {
        return runs;
    }

    public boolean isValid() {
        return valid;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        matchType = StringUtils.trimToNull(matchType);
        if (MATCH_TYPE_WILDCARD.equalsIgnoreCase(matchType)) {
            this.matchType = MATCH_TYPE_WILDCARD;
        } else {
            this.matchType = DEFAULT_MATCH_TYPE;
        }
    }

    public boolean isFilter() {
        return filter;
    }

    public boolean isNoSubstitution() {
        return noSubstitution;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String toString() {
        return "RuleBase{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", from='" + from + '\'' +
                '}';
    }
}
