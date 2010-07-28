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
import org.tuckey.web.filters.urlrewrite.utils.NumberUtils;
import org.tuckey.web.filters.urlrewrite.utils.RegexPattern;
import org.tuckey.web.filters.urlrewrite.utils.StringMatchingMatcher;
import org.tuckey.web.filters.urlrewrite.utils.StringMatchingPattern;
import org.tuckey.web.filters.urlrewrite.utils.StringMatchingPatternSyntaxException;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import org.tuckey.web.filters.urlrewrite.utils.WildcardPattern;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.Calendar;

/**
 * Conditions must be met when the filter is processing a url.
 *
 * @author Paul Tuckey
 * @version $Revision: 48 $ $Date: 2006-11-27 16:53:31 +1300 (Mon, 27 Nov 2006) $
 */
public class Condition extends TypeConverter {

    private static Log log = Log.getLog(Condition.class);

    /**
     * Should this expression be matched case sensitively.
     */
    private boolean caseSensitive = false;

    /**
     * Used to identify the condition.
     */
    private int id = 0;

    /**
     * Used to cache the pattern for faster execution.
     */
    private StringMatchingPattern pattern;

    /**
     * The name of the header etc.
     */
    private String name;

    /**
     * The operator that should be used to evaluate the condition.
     */
    private short operator;

    /**
     * Value of this condition if the type is header.
     */
    private String strValue;

    /**
     * Value of this condition if the type is port.
     */
    private long numericValue = 0;

    /**
     * What to do with the next rule, this will indicate "or" otherwise do "and".
     */
    private boolean processNextOr = false;

    private boolean valid = false;
    private boolean initialised = false;

    // Operators
    private static final short OPERATOR_EQUAL = 1;
    private static final short OPERATOR_NOT_EQUAL = 2;
    private static final short OPERATOR_GREATER_THAN = 3;
    private static final short OPERATOR_LESS_THAN = 4;
    private static final short OPERATOR_GREATER_THAN_OR_EQUAL = 5;
    private static final short OPERATOR_LESS_THAN_OR_EQUAL = 6;
    private static final short OPERATOR_INSTANCEOF = 7;
    private static final short OPERATOR_IS_DIR = 8;
    private static final short OPERATOR_IS_FILE = 9;
    private static final short OPERATOR_IS_FILE_WITH_SIZE = 10;
    private static final short OPERATOR_NOT_DIR = 11;
    private static final short OPERATOR_NOT_FILE = 12;
    private static final short OPERATOR_NOT_FILE_WITH_SIZE = 13;

    // if we are doing an instanceof test the class we want to test against
    Class instanceOfClass = null;

    // the rule that "owns" this condition.
    private RuleBase rule;


    /**
     * Will check and see if the condition matches the request.
     *
     * @param hsRequest
     * @return true on match
     * @deprecated use getConditionMatch(HttpServletRequest hsRequest)
     */
    public boolean matches(final HttpServletRequest hsRequest) {
        return getConditionMatch(hsRequest) != null;
    }

    /**
     * Will check and see if the condition matches the request.
     *
     * @param hsRequest
     * @return true on match
     */
    public ConditionMatch getConditionMatch(final HttpServletRequest hsRequest) {
        if (!initialised) {
            log.debug("condition not initialised skipping");
            // error initialising do not process
            return null;
        }
        if (!valid) {
            log.debug("condition not valid skipping");
            return null;
        }

        switch (type) {
            case TYPE_TIME:
                return evaluateNumericCondition(System.currentTimeMillis());
            case TYPE_TIME_YEAR:
                return evaluateCalendarCondition(Calendar.YEAR);
            case TYPE_TIME_MONTH:
                return evaluateCalendarCondition(Calendar.MONTH);
            case TYPE_TIME_DAY_OF_MONTH:
                return evaluateCalendarCondition(Calendar.DAY_OF_MONTH);
            case TYPE_TIME_DAY_OF_WEEK:
                return evaluateCalendarCondition(Calendar.DAY_OF_WEEK);
            case TYPE_TIME_AMPM:
                return evaluateCalendarCondition(Calendar.AM_PM);
            case TYPE_TIME_HOUR_OF_DAY:
                return evaluateCalendarCondition(Calendar.HOUR_OF_DAY);
            case TYPE_TIME_MINUTE:
                return evaluateCalendarCondition(Calendar.MINUTE);
            case TYPE_TIME_SECOND:
                return evaluateCalendarCondition(Calendar.SECOND);
            case TYPE_TIME_MILLISECOND:
                return evaluateCalendarCondition(Calendar.MILLISECOND);

            case TYPE_ATTRIBUTE:
                return evaluateAttributeCondition(name == null ? null : hsRequest.getAttribute(name));
            case TYPE_AUTH_TYPE:
                return evaluateStringCondition(hsRequest.getAuthType());
            case TYPE_CHARACTER_ENCODING:
                return evaluateStringCondition(hsRequest.getCharacterEncoding());
            case TYPE_CONTENT_LENGTH:
                return evaluateNumericCondition(hsRequest.getContentLength());
            case TYPE_CONTENT_TYPE:
                return evaluateStringCondition(hsRequest.getContentType());
            case TYPE_CONTEXT_PATH:
                return evaluateStringCondition(hsRequest.getContextPath());
            case TYPE_COOKIE:
                return evaluateCookieCondition(hsRequest.getCookies(), name);
            case TYPE_LOCAL_PORT:
                return evaluateNumericCondition(hsRequest.getLocalPort());
            case TYPE_METHOD:
                return evaluateStringCondition(hsRequest.getMethod());
            case TYPE_PARAMETER:
                return evaluateStringCondition(name == null ? null : hsRequest.getParameter(name));
            case TYPE_PATH_INFO:
                return evaluateStringCondition(hsRequest.getPathInfo());
            case TYPE_PATH_TRANSLATED:
                return evaluateStringCondition(hsRequest.getPathTranslated());
            case TYPE_PROTOCOL:
                return evaluateStringCondition(hsRequest.getProtocol());
            case TYPE_QUERY_STRING:
                return evaluateStringCondition(hsRequest.getQueryString());
            case TYPE_REMOTE_ADDR:
                return evaluateStringCondition(hsRequest.getRemoteAddr());
            case TYPE_REMOTE_HOST:
                return evaluateStringCondition(hsRequest.getRemoteHost());
            case TYPE_REMOTE_USER:
                return evaluateStringCondition(hsRequest.getRemoteUser());
            case TYPE_REQUESTED_SESSION_ID:
                return evaluateStringCondition(hsRequest.getRequestedSessionId());
            case TYPE_REQUESTED_SESSION_ID_FROM_COOKIE:
              return evaluateBoolCondition(hsRequest.isRequestedSessionIdFromCookie());
            case TYPE_REQUESTED_SESSION_ID_FROM_URL:
                return evaluateBoolCondition(hsRequest.isRequestedSessionIdFromURL());
            case TYPE_REQUESTED_SESSION_ID_VALID:
              return evaluateBoolCondition(hsRequest.isRequestedSessionIdValid());
            case TYPE_REQUEST_URI:
                return evaluateStringCondition(hsRequest.getRequestURI());
            case TYPE_REQUEST_URL:
                StringBuffer requestUrlBuff = hsRequest.getRequestURL();
                String requestUrlStr = null;
                if (requestUrlBuff != null) {
                    requestUrlStr = requestUrlBuff.toString();
                }
                return evaluateStringCondition(requestUrlStr);
            case TYPE_SESSION_ATTRIBUTE:
                Object sessionAttributeValue = null;
                final HttpSession session = hsRequest.getSession(false);
                if (session != null && name != null) {
                    sessionAttributeValue = session.getAttribute(name);
                }
                return evaluateAttributeCondition(sessionAttributeValue);

            case TYPE_SESSION_IS_NEW:
                boolean sessionNew = false;
                final HttpSession sessionIsNew = hsRequest.getSession(false);
                if (sessionIsNew != null) {
                    sessionNew = sessionIsNew.isNew();
                }
                return evaluateBoolCondition(sessionNew);
            case TYPE_SERVER_PORT:
                return evaluateNumericCondition(hsRequest.getServerPort());
            case TYPE_SERVER_NAME:
                return evaluateStringCondition(hsRequest.getServerName());
            case TYPE_SCHEME:
                return evaluateStringCondition(hsRequest.getScheme());
            case TYPE_USER_IN_ROLE:
                log.debug("is user in role " + name + " op " + operator);
                return evaluateBoolCondition(hsRequest.isUserInRole(name));

            case TYPE_EXCEPTION:
                String eName = null;
                Exception e = (Exception) hsRequest.getAttribute("javax.servlet.error.exception");

                if (OPERATOR_INSTANCEOF == operator) {
                    return evaluateInstanceOfCondition(e);
                } else {
                    if (e != null && e.getClass() != null) eName = e.getClass().getName();
                    return evaluateStringCondition(eName);
                }

            case TYPE_REQUEST_FILENAME:
                if ( rule.getServletContext() != null ) {
                    String fileName = rule.getServletContext().getRealPath(hsRequest.getRequestURI());
                    if ( log.isDebugEnabled() ) log.debug("fileName found is " + fileName);
                    return evaluateStringCondition(fileName);
                }   else {
                    log.error("unable to get servlet context for filename lookup, skipping");
                    return null;
                }

            default:
                return evaluateHeaderCondition(hsRequest);
        }
    }

    private ConditionMatch evaluateAttributeCondition(Object attribObject) {
        String attribValue = null;
        if (attribObject == null) {
            if (log.isDebugEnabled()) {
                log.debug(name + " doesn't exist");
            }
        } else {
            attribValue = attribObject.toString();
        }
        if (OPERATOR_INSTANCEOF == operator) {
            return evaluateInstanceOfCondition(attribObject);
        } else {
            return evaluateStringCondition(attribValue);
        }
    }

    private ConditionMatch evaluateInstanceOfCondition(Object obj) {
        // only test for instanceof if object is not null
        if (obj == null) return null;

        if (log.isDebugEnabled()) {
            log.debug("is " + obj.getClass() + " an instanceof " + instanceOfClass);
        }
        if (instanceOfClass == null) {
            log.error("this condition may have failed to initialise correctly, instanceof class is null");
            return null;
        }
        if (instanceOfClass.isInstance(obj)) {
            log.debug("yes");
            return new ConditionMatch();
        }
        log.debug("no");
        return null;
    }


    private ConditionMatch evaluateCookieCondition(Cookie[] cookies, String name) {
        if (cookies == null) {
            // we will have to do an exists check
            return evaluateBoolCondition(false);
        }
        if (name == null) {
            return evaluateBoolCondition(false);
        }
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie == null) {
                continue;
            }
            if (name.equals(cookie.getName())) {
                return evaluateStringCondition(cookie.getValue());
            }
        }
        return evaluateBoolCondition(false);
    }


    private ConditionMatch evaluateStringCondition(String value) {
        if (pattern == null && value == null) {
            log.debug("value is empty and pattern is also, condition false");
            return evaluateBoolCondition(false);
        }
        if ( operator == OPERATOR_IS_DIR ) {
            if ( log.isDebugEnabled() ) log.debug("checking to see if " + value + " is a directory");
            File fileToCheck = new File(value);
            return evaluateBoolCondition(fileToCheck.isDirectory());
        } else if ( operator == OPERATOR_IS_FILE ) {
            if ( log.isDebugEnabled() ) log.debug("checking to see if " + value + " is a file");
            File fileToCheck = new File(value);
            return evaluateBoolCondition(fileToCheck.isFile());
        } else if ( operator == OPERATOR_IS_FILE_WITH_SIZE ) {
            if ( log.isDebugEnabled() ) log.debug("checking to see if " + value + " is a file with size");
            File fileToCheck = new File(value);
            return evaluateBoolCondition(fileToCheck.isFile() && fileToCheck.length() > 0);
        } else if ( operator == OPERATOR_NOT_DIR ) {
            if ( log.isDebugEnabled() ) log.debug("checking to see if " + value + " is not a directory");
            File fileToCheck = new File(value);
            return evaluateBoolCondition(!fileToCheck.isDirectory());
        } else if ( operator == OPERATOR_NOT_FILE ) {
            if ( log.isDebugEnabled() ) log.debug("checking to see if " + value + " is not a file");
            File fileToCheck = new File(value);
            return evaluateBoolCondition(!fileToCheck.isFile());
        } else if ( operator == OPERATOR_NOT_FILE_WITH_SIZE ) {
            if ( log.isDebugEnabled() ) log.debug("checking to see if " + value + " is not a file with size");
            File fileToCheck = new File(value);
            return evaluateBoolCondition(!(fileToCheck.isFile() && fileToCheck.length() > 0));
        }
        if (pattern == null) {
            log.debug("value isn't empty but pattern is, assuming checking for existence, condition true");
            return evaluateBoolCondition(true);
        }
        if (value == null) {
            // value is null make value ""
            value = "";
        }
        if (log.isDebugEnabled()) {
            log.debug("evaluating \"" + value + "\" against " + strValue);
        }
        StringMatchingMatcher matcher = pattern.matcher(value);
        return evaluateBoolCondition(matcher, matcher.find());
    }

    /**
     * Evaluate taking into account the operator, not only boolean operators considered.
     */
    private ConditionMatch evaluateBoolCondition(boolean outcome) {
        if (log.isTraceEnabled()) {
            log.trace("outcome " + outcome);
        }
        if (operator == OPERATOR_NOT_EQUAL) {
            log.debug("not equal operator in use");
            return !outcome ? new ConditionMatch() : null;
        }
        return outcome ? new ConditionMatch() : null;
    }

    private ConditionMatch evaluateBoolCondition(StringMatchingMatcher matcher, boolean outcome) {
        ConditionMatch conditionMatch = evaluateBoolCondition(outcome);
        if (conditionMatch != null) {
            conditionMatch.setMatcher(matcher);
        }
        return conditionMatch;
    }

    private ConditionMatch evaluateHeaderCondition(final HttpServletRequest hsRequest) {
        String headerValue = null;
        if (name != null) {
            headerValue = hsRequest.getHeader(name);
        }
        return evaluateStringCondition(headerValue);
    }

    /**
     * Will evaluate a calendar condition.
     *
     * @param calField the calendar field from Calendar
     */
    private ConditionMatch evaluateCalendarCondition(final int calField) {
        return evaluateNumericCondition((Calendar.getInstance()).get(calField));
    }

    /**
     * Will evaluate usign operator.
     *
     * @param compareWith what to compare with
     * @return true or false
     */
    private ConditionMatch evaluateNumericCondition(final long compareWith) {
        if (log.isDebugEnabled()) {
            log.debug("evaluating with operator, is " + compareWith + " " + getOperator() + " " + numericValue);
        }
        switch (operator) {
            case OPERATOR_NOT_EQUAL:
                return compareWith != numericValue ? new ConditionMatch() : null;
            case OPERATOR_GREATER_THAN:
                return compareWith > numericValue ? new ConditionMatch() : null;
            case OPERATOR_LESS_THAN:
                return compareWith < numericValue ? new ConditionMatch() : null;
            case OPERATOR_GREATER_THAN_OR_EQUAL:
                return compareWith >= numericValue ? new ConditionMatch() : null;
            case OPERATOR_LESS_THAN_OR_EQUAL:
                return compareWith <= numericValue ? new ConditionMatch() : null;
            default:
                return compareWith == numericValue ? new ConditionMatch() : null;
        }
    }


    /**
     * Returns false on failure. Use getError to get the description of the error.
     *
     * @return weather or not the condition was successful in initialisation.
     */
    public boolean initialise() {
        initialised = true;
        if (error != null) {
            return false;
        }
        // make sure we default to header if not set
        if (type == 0) {
            type = TYPE_HEADER;
        }
        switch (type) {
            // note, only numeric specified others handled by default:
            case TYPE_SERVER_PORT:
                initNumericValue();
                break;
            case TYPE_TIME:
                initNumericValue();
                break;
            case TYPE_TIME_YEAR:
                initNumericValue();
                break;
            case TYPE_TIME_MONTH:
                initNumericValue();
                break;
            case TYPE_TIME_DAY_OF_MONTH:
                initNumericValue();
                break;
            case TYPE_TIME_DAY_OF_WEEK:
                initNumericValue();
                break;
            case TYPE_TIME_AMPM:
                initNumericValue();
                break;
            case TYPE_TIME_HOUR_OF_DAY:
                initNumericValue();
                break;
            case TYPE_TIME_MINUTE:
                initNumericValue();
                break;
            case TYPE_TIME_SECOND:
                initNumericValue();
                break;
            case TYPE_TIME_MILLISECOND:
                initNumericValue();
                break;
            case TYPE_CONTENT_LENGTH:
                initNumericValue();
                break;
            case TYPE_LOCAL_PORT:
                initNumericValue();
                break;
            case TYPE_USER_IN_ROLE:
                // we only care to make sure the user has entered a name (if no name use value)
                // note regexs cannot be entered against this due to limitations in servlet spec
                if (StringUtils.isBlank(name)) {
                    name = strValue;
                }
                break;
            case TYPE_SESSION_ATTRIBUTE:
                if (StringUtils.isBlank(name)) {
                    setError("you must set a name for session attributes");
                }
                initStringValue();
                break;
            case TYPE_ATTRIBUTE:
                if (StringUtils.isBlank(name)) {
                    setError("you must set a name for attributes");
                }
                initStringValue();
                break;
            case TYPE_HEADER:
                if (StringUtils.isBlank(name)) {
                    setError("you must set a name for a header");
                }
                initStringValue();
                break;
            default:
                // other generic types
                initStringValue();
        }
        if (log.isDebugEnabled()) {
            log.debug("loaded condition " + getType() + " " + name + " " + strValue);
        }
        valid = error == null;
        return valid;
    }

    private void initStringValue() {
        if (StringUtils.isBlank(strValue)) {
            log.debug("value is blank initing pattern to null");
            pattern = null;
            return;
        }
        if (OPERATOR_INSTANCEOF == operator) {
            // want to be able to do instance of that means value is not a regexp
            log.debug("initialising instanceof condition");
            strValue = StringUtils.trim(strValue);
            try {
                instanceOfClass = Class.forName(strValue);
            } catch (ClassNotFoundException e) {
                setError("could not find " + strValue + " got a " + e.toString());
            } catch (NoClassDefFoundError e) {
                setError("could not find " + strValue + " got a " + e.toString());
            }

        } else {
            try {
                if (rule != null && rule.isMatchTypeWildcard()) {
                    log.debug("rule match type is wildcard");
                    pattern = new WildcardPattern(strValue);

                } else {
                    // default is regex
                    pattern = new RegexPattern(strValue, caseSensitive);
                }

            } catch (StringMatchingPatternSyntaxException e) {
                setError("Problem compiling regular expression " + strValue + " (" + e.getMessage() + ")");
            }
        }
    }

    /**
     * Will init a numeric value type ie port.
     */
    private void initNumericValue() {
        if (numericValue == 0) {
            numericValue = NumberUtils.stringToLong(StringUtils.trim(strValue));
            if (numericValue == 0 && !"0".equals(strValue)) {
                setError("Value " + strValue + " is not a valid number (tried to cast to java type long)");
            }
        }
    }

    protected void setError(String s) {
        super.setError(s);
        log.error("Condition " + id + " had error: " + s);
    }


    /**
     * Will get the operator type.
     *
     * @return notequal, greater etc.
     */
    public String getOperator() {
        switch (operator) {
            case OPERATOR_NOT_EQUAL:
                return "notequal";
            case OPERATOR_GREATER_THAN:
                return "greater";
            case OPERATOR_LESS_THAN:
                return "less";
            case OPERATOR_GREATER_THAN_OR_EQUAL:
                return "greaterorequal";
            case OPERATOR_LESS_THAN_OR_EQUAL:
                return "lessorequal";
            case OPERATOR_INSTANCEOF:
                return "instanceof";
            case OPERATOR_EQUAL:
                return "equal";
            case OPERATOR_IS_DIR:
                return "isdir";
            case OPERATOR_IS_FILE:
                return "isfile";
            case OPERATOR_IS_FILE_WITH_SIZE:
                return "isfilewithsize";
            case OPERATOR_NOT_DIR:
                return "notdir";
            case OPERATOR_NOT_FILE:
                return "notfile";
            case OPERATOR_NOT_FILE_WITH_SIZE:
                return "notfilewithsize";
            default:
                return "";
        }
    }

    /**
     * Will ste the operator.
     *
     * @param operator type
     */
    public void setOperator(final String operator) {
        if ("notequal".equals(operator)) {
            this.operator = OPERATOR_NOT_EQUAL;
        } else if ("greater".equals(operator)) {
            this.operator = OPERATOR_GREATER_THAN;
        } else if ("less".equals(operator)) {
            this.operator = OPERATOR_LESS_THAN;
        } else if ("greaterorequal".equals(operator)) {
            this.operator = OPERATOR_GREATER_THAN_OR_EQUAL;
        } else if ("lessorequal".equals(operator)) {
            this.operator = OPERATOR_LESS_THAN_OR_EQUAL;
        } else if ("instanceof".equals(operator)) {
            this.operator = OPERATOR_INSTANCEOF;
        } else if ("equal".equals(operator) || StringUtils.isBlank(operator)) {
            this.operator = OPERATOR_EQUAL;
        } else if ("isdir".equals(operator)) {
            this.operator = OPERATOR_IS_DIR;
        } else if ("isfile".equals(operator)) {
            this.operator = OPERATOR_IS_FILE;
        } else if ("isfilewithsize".equals(operator)) {
            this.operator = OPERATOR_IS_FILE_WITH_SIZE;
        } else if ("notdir".equals(operator)) {
            this.operator = OPERATOR_NOT_DIR;
        } else if ("notfile".equals(operator)) {
            this.operator = OPERATOR_NOT_FILE;
        } else if ("notfilewithsize".equals(operator)) {
            this.operator = OPERATOR_NOT_FILE_WITH_SIZE;
        } else {
            setError("Operator " + operator + " is not valid");
        }
    }

    /**
     * Will get the name.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Will set the name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Will return "add" or "or".
     *
     * @return "add" or "or"
     */
    public String getNext() {
        if (processNextOr) return "or";
        return "and";
    }

    /**
     * Will set next.
     *
     * @param next "or" or "and"
     */
    public void setNext(final String next) {
        if ("or".equals(next)) {
            this.processNextOr = true;
        } else if ("and".equals(next) || StringUtils.isBlank(next)) {
            this.processNextOr = false;
        } else {
            setError("Next " + next + " is not valid (can be 'and', 'or')");
        }
    }

    /**
     * Will get the value.
     *
     * @return String
     */
    public String getValue() {
        return strValue;
    }

    /**
     * Will set the value.
     *
     * @param value the value
     */
    public void setValue(final String value) {
        this.strValue = value;
    }

    /**
     * True if process next is or.
     *
     * @return boolean
     */
    public boolean isProcessNextOr() {
        return processNextOr;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String getDisplayName() {
        return "Condtition " + id;
    }

    public void setRule(RuleBase rule) {
        this.rule = rule;
    }
}
