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
    private Cookie cookie;

    private static final short SET_TYPE_REQUEST = 0;
    private static final short SET_TYPE_SESSION = 1;
    private static final short SET_TYPE_RESPONSE_HEADER = 2;
    private static final short SET_TYPE_COOKIE = 3;
    private static final short SET_TYPE_CONTENT_TYPE = 4;
    private static final short SET_TYPE_CHARSET = 5;
    private static final short SET_TYPE_LOCALE = 6;
    private static final short SET_TYPE_STAUS = 7;
    private static final short SET_TYPE_PARAM = 8;

    private boolean valueContainsVariable = false;
    private boolean valueContainsBackRef = false;
    private static Pattern replacementVarPattern = Pattern.compile("(?<!\\\\)\\$([0-9])");

    public String getType() {
        if (type == SET_TYPE_RESPONSE_HEADER) return "response-header";
        if (type == SET_TYPE_SESSION) return "session";
        if (type == SET_TYPE_COOKIE) return "cookie";
        if (type == SET_TYPE_CONTENT_TYPE) return "content-type";
        if (type == SET_TYPE_CHARSET) return "charset";
        if (type == SET_TYPE_LOCALE) return "locale";
        if (type == SET_TYPE_STAUS) return "status";
        if (type == SET_TYPE_PARAM) return "param";
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
        } else if ("param".equals(typeStr) || "parameter".equals(typeStr)) {
            type = SET_TYPE_PARAM;
        } else if ("request".equals(typeStr) || StringUtils.isBlank(typeStr)) {
            type = SET_TYPE_REQUEST;
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

    /**
     * @see #execute(ConditionMatch, StringMatchingMatcher, HttpServletRequest, HttpServletResponse) execute
     */
    public void execute(HttpServletRequest hsRequest, HttpServletResponse hsResponse) {
        execute(null, null, hsRequest, hsResponse);
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
        if (valueContainsBackRef) {
            value = BackReferenceReplacer.replace(lastConditionMatch, value);
        }
        if (valueContainsVariable) {
            value = VariableReplacer.replace(value, hsRequest);
        }
        if (toMatcher != null) {
            Matcher replacementVarMatcher = replacementVarPattern.matcher(value);
            if (replacementVarMatcher.find()) {
                value = toMatcher.replaceAll(value);
            }
        }

        if (type == SET_TYPE_REQUEST) {
            log.debug("setting request attrib");
            hsRequest.setAttribute(name, value);

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
            log.debug("setting status");
            hsResponse.addCookie(cookie);

        } else if (type == SET_TYPE_CONTENT_TYPE) {
            log.debug("setting content type");
            hsResponse.setContentType(value);

        } else if (type == SET_TYPE_CHARSET) {
            log.debug("setting charset");
            hsResponse.setCharacterEncoding(value);

        } else if (type == SET_TYPE_LOCALE) {
            log.debug("setting charset");
            hsResponse.setLocale(locale);

        } else if (type == SET_TYPE_PARAM) {
            if ( hsRequest instanceof UrlRewriteWrappedRequest ) {
                log.debug("setting parameter");
                ((UrlRewriteWrappedRequest) hsRequest).setParameter(name, value);
            }   else {
                log.debug("not setting parameter as request is not wrapped");
            }

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
            cookie = null;
            // VAL[:domain[:lifetime[:path]]]
            if (value != null && name != null) {
                if (value.indexOf(":") != -1) {
                    // we must have extra items
                    String items[] = value.split(":");
                    cookie = new Cookie(name, items[0]);
                    if (items.length > 1) cookie.setDomain(items[1]);
                    if (items.length > 2) cookie.setMaxAge(NumberUtils.stringToInt(items[2]));
                    if (items.length > 3) cookie.setPath(items[3]);

                } else {
                    cookie = new Cookie(name, value);
                }
            } else {
                setError("cookie must have a name and a value");
            }
        }

        if (error == null) {
            valid = true;
        }
        return valid;
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
