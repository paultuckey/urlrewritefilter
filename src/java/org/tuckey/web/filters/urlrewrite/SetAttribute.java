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

import org.tuckey.web.filters.urlrewrite.substitution.BackReferenceReplacer;
import org.tuckey.web.filters.urlrewrite.substitution.ChainedSubstitutionFilters;
import org.tuckey.web.filters.urlrewrite.substitution.FunctionReplacer;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionContext;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionFilterChain;
import org.tuckey.web.filters.urlrewrite.substitution.VariableReplacer;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.NumberUtils;
import org.tuckey.web.filters.urlrewrite.utils.StringMatchingMatcher;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paul Tuckey
 * @version $Revision: 12 $ $Date: 2006-08-20 20:53:09 +1200 (Sun, 20 Aug 2006) $
 */
public class SetAttribute {

    private static Log log = Log.getLog(SetAttribute.class);

    private boolean initialised = false;
    private boolean valid = false;

    /**
     * Error message from the regular expression compilation.
     */
    private String error = null;

    private short type;
    private String name;
    private String value;
    private int numericValue;
    private Locale locale;

    private static final short SET_TYPE_REQUEST = 0;
    private static final short SET_TYPE_SESSION = 1;
    private static final short SET_TYPE_RESPONSE_HEADER = 2;
    private static final short SET_TYPE_COOKIE = 3;
    private static final short SET_TYPE_CONTENT_TYPE = 4;
    private static final short SET_TYPE_CHARSET = 5;
    private static final short SET_TYPE_LOCALE = 6;
    private static final short SET_TYPE_STAUS = 7;
    private static final short SET_TYPE_PARAM = 8;
    private static final short SET_TYPE_EXPIRES = 9;
    private static final short SET_TYPE_METHOD = 10;

    private long expiresValueAdd = 0;
    private boolean valueContainsVariable = false;
    private boolean valueContainsBackRef = false;
    private boolean valueContainsFunction = false;

    public String getType() {
        if (type == SET_TYPE_RESPONSE_HEADER) return "response-header";
        if (type == SET_TYPE_SESSION) return "session";
        if (type == SET_TYPE_COOKIE) return "cookie";
        if (type == SET_TYPE_CONTENT_TYPE) return "content-type";
        if (type == SET_TYPE_CHARSET) return "charset";
        if (type == SET_TYPE_LOCALE) return "locale";
        if (type == SET_TYPE_STAUS) return "status";
        if (type == SET_TYPE_PARAM) return "parameter";
        if (type == SET_TYPE_EXPIRES) return "expires";
        if (type == SET_TYPE_METHOD) return "method";
        return "request";
    }

    public void setType(String typeStr) {
        if ("response-header".equals(typeStr)) {
            type = SET_TYPE_RESPONSE_HEADER;
        } else if ("session".equals(typeStr)) {
            type = SET_TYPE_SESSION;
        } else if ("cookie".equals(typeStr)) {
            type = SET_TYPE_COOKIE;
        } else if ("content-type".equals(typeStr)) {
            type = SET_TYPE_CONTENT_TYPE;
        } else if ("charset".equals(typeStr)) {
            type = SET_TYPE_CHARSET;
        } else if ("locale".equals(typeStr)) {
            type = SET_TYPE_LOCALE;
        } else if ("status".equals(typeStr)) {
            type = SET_TYPE_STAUS;
        } else if ("parameter".equals(typeStr) || "param".equals(typeStr)) {
            type = SET_TYPE_PARAM;
        } else if ("expires".equals(typeStr)) {
            type = SET_TYPE_EXPIRES;
        } else if ("request".equals(typeStr) || StringUtils.isBlank(typeStr)) {
            type = SET_TYPE_REQUEST;
        } else if ("method".equals(typeStr)) {
            type = SET_TYPE_METHOD;
        } else {
            setError("type (" + typeStr + ") is not valid");
        }
    }

    private void setError(String s) {
        log.error("set " + getDisplayName() + " had error: " + s);
        error = s;
    }

    public String getError() {
        return error;
    }

    public String getDisplayName() {
        return "Set " + getType() + " " + name + " " + value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void execute(ConditionMatch lastConditionMatch, StringMatchingMatcher toMatcher,
                        HttpServletRequest hsRequest, HttpServletResponse hsResponse) {

        if (log.isDebugEnabled()) {
            log.debug("set " + getDisplayName() + " called");
        }
        if (!valid) {
            log.debug("not valid, skipping");
            return;
        }
        if (!initialised) {
            log.debug("not initialised, skipping");
            return;
        }

        String value = this.value;
        
        SubstitutionContext substitutionContext = new SubstitutionContext(hsRequest, toMatcher, lastConditionMatch, null);
        SubstitutionFilterChain substitutionFilter = ChainedSubstitutionFilters.getDefaultSubstitutionChain(false, valueContainsFunction, valueContainsVariable, valueContainsBackRef);
        value = substitutionFilter.substitute(value, substitutionContext);

        if (type == SET_TYPE_REQUEST) {
            log.debug("setting request attrib");
            hsRequest.setAttribute(name, value);

        } else if (type == SET_TYPE_METHOD) {
            log.debug("setting request method");
            if ( hsResponse instanceof UrlRewriteWrappedResponse ) {
                ((UrlRewriteWrappedResponse) hsResponse).setOverridenMethod(value);
            }   else {
                log.warn("unable to set request method as request not a UrlRewriteWrappedResponse");
            }

        } else if (type == SET_TYPE_PARAM) {
            log.debug("setting request parameter");
            if ( hsResponse instanceof UrlRewriteWrappedResponse ) {
                ((UrlRewriteWrappedResponse) hsResponse).addOverridenRequestParameter(name, value);
            }   else {
                log.warn("unable to set request parameter as request not a UrlRewriteWrappedResponse");
            }

        } else if (type == SET_TYPE_SESSION) {
            log.debug("setting session attrib");
            HttpSession session = hsRequest.getSession(true);
            if (session == null) {
                log.warn("could not create a new session for a request");
            } else {
                session.setAttribute(name, value);
            }

        } else if (type == SET_TYPE_RESPONSE_HEADER) {
            log.debug("setting response header");
            hsResponse.addHeader(name, value);

        } else if (type == SET_TYPE_STAUS) {
            log.debug("setting status");
            hsResponse.setStatus(numericValue);

        } else if (type == SET_TYPE_COOKIE) {
            Cookie cookieToAdd = getCookie(name, value);
            if ( cookieToAdd != null ) {
                log.debug("adding cookie");
                hsResponse.addCookie(cookieToAdd);
            }

        } else if (type == SET_TYPE_CONTENT_TYPE) {
            log.debug("setting content type");
            hsResponse.setContentType(value);

        } else if (type == SET_TYPE_CHARSET) {
            log.debug("setting charset");
            hsResponse.setCharacterEncoding(value);

        } else if (type == SET_TYPE_LOCALE) {
            log.debug("setting charset");
            hsResponse.setLocale(locale);

        } else if (type == SET_TYPE_EXPIRES) {
            log.debug("setting expires");
            hsResponse.setDateHeader("Expires", System.currentTimeMillis() + expiresValueAdd);

        } else {
            log.warn("unknown type" + type);
        }

    }

    public boolean initialise() {
        initialised = true;

        if (value != null) {
            if (BackReferenceReplacer.containsBackRef(value)) {
                valueContainsBackRef = true;
            }
            if (VariableReplacer.containsVariable(value)) {
                valueContainsVariable = true;
            }
            if (FunctionReplacer.containsFunction(value)) {
                valueContainsFunction = true;
            }
        }

        if (type == SET_TYPE_STAUS) {
            initNumericValue();
        } else if (type == SET_TYPE_LOCALE) {
            // value might be zh-CN-abcdef or zh-CN or zh
            locale = null;
            if (value == null) {
                setError("Locale is not valid because value is null");
            } else if (value.matches("[a-zA-Z][a-zA-Z]")) {
                locale = new Locale(value);
            } else if (value.matches("[a-zA-Z][a-zA-Z]-[a-zA-Z][a-zA-Z]")) {
                locale = new Locale(value.substring(1, 2), value.substring(2, 4));
            } else if (value.matches("[a-zA-Z][a-zA-Z]-[a-zA-Z][a-zA-Z]-.*")) {
                locale = new Locale(value.substring(1, 2), value.substring(4, 5), value.substring(6, value.length()));
            } else {
                setError("Locale " + value + " is not valid (valid locales are, zh, zh-CN, zh-CN-rural)");
            }

        } else if (type == SET_TYPE_COOKIE) {
            // VAL[:domain[:lifetime[:path]]]
            if (value != null && name != null) {
                getCookie(name, value);
            } else {
                setError("cookie must have a name and a value");
            }

        } else if (type == SET_TYPE_EXPIRES) {
            // "access plus 1 month"
            if (value != null ) {
                expiresValueAdd = parseTimeValue(value);
            } else {
                setError("expires must have a value");
            }
        }

        if (error == null) {
            valid = true;
        }
        return valid;
    }

    /**
     * takes a string a number expression and converts it to a long.
     * syntax: number type
     *
     * Valid examples: "1 day", "2 days", "1 hour", "1 hour 2 minutes", "34 months"
     *
     * Any positive number is valid
     *
     * Valid types are: years, months, weeks, days, hours, minutes, seconds
     *
     * note, this syntax is a direct copy of mod_expires syntax
     * http://httpd.apache.org/docs/2.0/mod/mod_expires.html
     *
     * note, a year is calculated as 365.25 days and a month as 365.25 days divided by 12.
     */
    protected long parseTimeValue(String parsingValue) {
        long calculatedMillis = 0;
        if ( parsingValue.startsWith("access")) parsingValue = parsingValue.substring("access".length()).trim();
        if ( parsingValue.startsWith("plus")) parsingValue = parsingValue.substring("plus".length()).trim();
        log.debug("calculating expires ms based on '" + parsingValue + "'");
        Matcher matcher = Pattern.compile("([0-9]+)\\s+(\\w+)").matcher(parsingValue);
        while ( matcher.find()) {
            long num = NumberUtils.stringToInt(matcher.group(1), -1);
            if ( num < 0 ) setError("could not calculate numeric value of " + matcher.group(1));
            String part = matcher.group(2);
            log.debug("adding '"+num+"' '" + part + "'");
            long addThisRound = 0;
            if ( part.matches("year[s]?") ) addThisRound = num * Math.round(1000 * 60 * 60 * 24 * 365.25);
            if ( part.matches("month[s]?") ) addThisRound = num * Math.round( 1000 * 60 * 60 * 24 * (365.25/12) );
            if ( part.matches("week[s]?") ) addThisRound = num * ( 1000 * 60 * 60 * 24 * 7 );
            if ( part.matches("day[s]?") ) addThisRound = num * ( 1000 * 60 * 60 * 24 );
            if ( part.matches("hour[s]?") ) addThisRound = num * ( 1000 * 60 * 60 );
            if ( part.matches("minute[s]?") ) addThisRound = num * ( 1000 * 60 );
            if ( part.matches("second[s]?") ) addThisRound = num * ( 1000 );
            if ( addThisRound == 0 ) {
                setError("unkown time unit '" + part + "'");
            }
            calculatedMillis += addThisRound;
        }
        if ( calculatedMillis == 0 ) {
            setError("could not calculate expires time from '"+parsingValue+"'");
        }
        return calculatedMillis;
    }

    private Cookie getCookie(String name, String value) {
        if ( log.isDebugEnabled() ) {
            log.debug("making cookie for " + name + ", " + value);
        }
        if ( name == null ) {
            log.info("getCookie called with null name");
            return null;
        }
        Cookie cookie;
        if (value != null && value.indexOf(":") != -1) {
            // we must have extra items
            String items[] = value.split(":");
            cookie = new Cookie(name, items[0]);
            if (items.length > 1) cookie.setDomain(items[1]);
            if (items.length > 2) cookie.setMaxAge(NumberUtils.stringToInt(items[2]));
            if (items.length > 3) cookie.setPath(items[3]);

        } else {
            cookie = new Cookie(name, value);
        }
        return cookie;
    }

    /**
     * Will init a numeric value type ie port.
     */
    private void initNumericValue() {
        if (numericValue == 0) {
            numericValue = NumberUtils.stringToInt(StringUtils.trim(value));
            if (numericValue == 0 && !"0".equals(value)) {
                setError("Value " + value + " is not a valid number (tried to cast to java type long)");
            }
        }
    }


}
