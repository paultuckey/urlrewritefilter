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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for variable replacement.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class VariableReplacer {

    private static Log log = Log.getLog(VariableReplacer.class);

    private static Pattern toVariablePattern = Pattern.compile("(?<!\\\\)%\\{(.*?)\\}");

    public static boolean containsVariable(String to) {
        Matcher variableMatcher = toVariablePattern.matcher(to);
        return variableMatcher.find();
    }


    public static String replace(String subjectOfReplacement, HttpServletRequest hsRequest) {
        Matcher varMatcher = toVariablePattern.matcher(subjectOfReplacement);
        StringBuffer sb = new StringBuffer();
        boolean anyMatches = false;

        while (varMatcher.find()) {
            anyMatches = true;
            int groupCount = varMatcher.groupCount();
            if (groupCount < 1) {
                log.error("group count on backref finder regex is not as expected");
                if (log.isDebugEnabled()) {
                    log.error("varMatcher: " + varMatcher.toString());
                }
                continue;
            }
            String varStr = varMatcher.group(1);
            String varValue = "";
            if (varStr != null) {
                varValue = varReplace(varStr, hsRequest);
                if (log.isDebugEnabled()) log.debug("resolved to: " + varValue);
            } else {
                if (log.isDebugEnabled()) log.debug("variable reference is null " + varMatcher);
            }
            varMatcher.appendReplacement(sb, varValue);
        }
        if (anyMatches) {
            varMatcher.appendTail(sb);
            log.debug("replaced sb is " + sb);
            return sb.toString();
        }
        return subjectOfReplacement;
    }

    /**
     * Handles the fetching of the variable value from the request.
     */
    private static String varReplace(String originalVarStr, HttpServletRequest hsRequest) {
        // get the sub name if any ie for headers etc header:user-agent
        String varSubName = null;
        String varType;
        int colonIdx = originalVarStr.indexOf(":");
        if (colonIdx != -1 && colonIdx + 1 < originalVarStr.length()) {
            varSubName = originalVarStr.substring(colonIdx + 1);
            varType = originalVarStr.substring(0, colonIdx);
            if (log.isDebugEnabled()) log.debug("variable %{" + originalVarStr + "} type: " + varType +
                    ", name: '" + varSubName + "'");
        } else {
            varType = originalVarStr;
            if (log.isDebugEnabled()) log.debug("variable %{" + originalVarStr + "} type: " + varType);
        }

        TypeConverter type = new TypeConverter();
        type.setType(varType);

        switch (type.getTypeShort()) {
            case TypeConverter.TYPE_TIME:
                return String.valueOf(System.currentTimeMillis());
            case TypeConverter.TYPE_TIME_YEAR:
                return calendarVariable(Calendar.YEAR);
            case TypeConverter.TYPE_TIME_MONTH:
                return calendarVariable(Calendar.MONTH);
            case TypeConverter.TYPE_TIME_DAY_OF_MONTH:
                return calendarVariable(Calendar.DAY_OF_MONTH);
            case TypeConverter.TYPE_TIME_DAY_OF_WEEK:
                return calendarVariable(Calendar.DAY_OF_WEEK);
            case TypeConverter.TYPE_TIME_AMPM:
                return calendarVariable(Calendar.AM_PM);
            case TypeConverter.TYPE_TIME_HOUR_OF_DAY:
                return calendarVariable(Calendar.HOUR_OF_DAY);
            case TypeConverter.TYPE_TIME_MINUTE:
                return calendarVariable(Calendar.MINUTE);
            case TypeConverter.TYPE_TIME_SECOND:
                return calendarVariable(Calendar.SECOND);
            case TypeConverter.TYPE_TIME_MILLISECOND:
                return calendarVariable(Calendar.MILLISECOND);

            case TypeConverter.TYPE_ATTRIBUTE:
                return attributeVariable(varSubName == null ? null : hsRequest.getAttribute(varSubName), varSubName);
            case TypeConverter.TYPE_AUTH_TYPE:
                return StringUtils.notNull(hsRequest.getAuthType());
            case TypeConverter.TYPE_CHARACTER_ENCODING:
                return StringUtils.notNull(hsRequest.getCharacterEncoding());
            case TypeConverter.TYPE_CONTENT_LENGTH:
                return String.valueOf(hsRequest.getContentLength());
            case TypeConverter.TYPE_CONTENT_TYPE:
                return StringUtils.notNull(hsRequest.getContentType());
            case TypeConverter.TYPE_CONTEXT_PATH:
                return StringUtils.notNull(hsRequest.getContextPath());
            case TypeConverter.TYPE_COOKIE:
                return cookieVariable(hsRequest.getCookies(), varSubName);
            case TypeConverter.TYPE_LOCAL_PORT:
                return String.valueOf(hsRequest.getLocalPort());
            case TypeConverter.TYPE_METHOD:
                return StringUtils.notNull(hsRequest.getMethod());
            case TypeConverter.TYPE_PARAMETER:
                return StringUtils.notNull(varSubName == null ? null : hsRequest.getParameter(varSubName));
            case TypeConverter.TYPE_PATH_INFO:
                return StringUtils.notNull(hsRequest.getPathInfo());
            case TypeConverter.TYPE_PATH_TRANSLATED:
                return StringUtils.notNull(hsRequest.getPathTranslated());
            case TypeConverter.TYPE_PROTOCOL:
                return StringUtils.notNull(hsRequest.getProtocol());
            case TypeConverter.TYPE_QUERY_STRING:
                return StringUtils.notNull(hsRequest.getQueryString());
            case TypeConverter.TYPE_REMOTE_ADDR:
                return StringUtils.notNull(hsRequest.getRemoteAddr());
            case TypeConverter.TYPE_REMOTE_HOST:
                return StringUtils.notNull(hsRequest.getRemoteHost());
            case TypeConverter.TYPE_REMOTE_USER:
                return StringUtils.notNull(hsRequest.getRemoteUser());
            case TypeConverter.TYPE_REQUESTED_SESSION_ID:
                return StringUtils.notNull(hsRequest.getRequestedSessionId());
            case TypeConverter.TYPE_REQUEST_URI:
                return StringUtils.notNull(hsRequest.getRequestURI());
            case TypeConverter.TYPE_REQUEST_URL:
                StringBuffer requestUrlBuff = hsRequest.getRequestURL();
                String requestUrlStr = null;
                if (requestUrlBuff != null) {
                    requestUrlStr = requestUrlBuff.toString();
                }
                return StringUtils.notNull(requestUrlStr);
            case TypeConverter.TYPE_SESSION_ATTRIBUTE:
                Object sessionAttributeValue = null;
                if (hsRequest.getSession() != null && varSubName != null) {
                    sessionAttributeValue = hsRequest.getSession().getAttribute(varSubName);
                }
                return attributeVariable(sessionAttributeValue, varSubName);

            case TypeConverter.TYPE_SESSION_IS_NEW:
                boolean sessionNew = false;
                HttpSession session = hsRequest.getSession();
                if (session != null) {
                    sessionNew = session.isNew();
                }
                return String.valueOf(sessionNew);
            case TypeConverter.TYPE_SERVER_PORT:
                return String.valueOf(hsRequest.getServerPort());
            case TypeConverter.TYPE_SERVER_NAME:
                return StringUtils.notNull(hsRequest.getServerName());
            case TypeConverter.TYPE_SCHEME:
                return StringUtils.notNull(hsRequest.getScheme());
            case TypeConverter.TYPE_USER_IN_ROLE:
                return String.valueOf(hsRequest.isUserInRole(varSubName));

            case TypeConverter.TYPE_EXCEPTION:
                Exception e = (Exception) hsRequest.getAttribute("javax.servlet.error.exception");
                if (e == null) return "";
                if (e.getClass() == null) return "";
                return e.getClass().getName();

            case TypeConverter.TYPE_HEADER:
                return StringUtils.notNull(hsRequest.getHeader(varSubName));

            default:
                log.error("variable %{" + originalVarStr + "} type '" + varType + "' not a valid type");
                return "";

        }
    }


    private static String attributeVariable(Object attribObject, String name) {
        String attribValue = null;
        if (attribObject == null) {
            if (log.isDebugEnabled()) {
                log.debug(name + " doesn't exist");
            }
        } else {
            attribValue = attribObject.toString();
        }
        return StringUtils.notNull(attribValue);
    }

    private static String cookieVariable(Cookie[] cookies, String name) {
        if (cookies == null) {
            // we will have to do an exists check
            return "";
        }
        if (name == null) {
            return "";
        }
        for (int i = 0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie == null) {
                continue;
            }
            if (name.equals(cookie.getName())) {
                return StringUtils.notNull(cookie.getValue());
            }
        }
        return null;
    }

    private static String calendarVariable(final int calField) {
        return String.valueOf((Calendar.getInstance()).get(calField));
    }


}
