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
package org.tuckey.web.filters.urlrewrite.substitution;

import org.tuckey.web.filters.urlrewrite.TypeConverter;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for variable replacement.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class VariableReplacer implements SubstitutionFilter {

    private static Log log = Log.getLog(VariableReplacer.class);

    private static Pattern toVariablePattern = Pattern.compile("(?<!\\\\)%\\{([-a-zA-Z:]*)\\}");

    private static ServletContext servletContext;
    
    public static boolean containsVariable(String to) {
        Matcher variableMatcher = toVariablePattern.matcher(to);
        return variableMatcher.find();
    }

	public VariableReplacer() {
    	
    }
    
    public VariableReplacer(ServletContext sc){
		if (sc == null) {
			throw new IllegalArgumentException("Servlet context is null");
		}
    	servletContext = sc;
    }
    
    public static String replace(String subjectOfReplacement, HttpServletRequest hsRequest) {
        return new VariableReplacer().substitute(subjectOfReplacement, new SubstitutionContext(hsRequest, null, null, null), new ChainedSubstitutionFilters(Collections.EMPTY_LIST));
    }

    public static String replaceWithServletContext(String subjectOfReplacement, HttpServletRequest hsRequest, ServletContext sc) {
        return new VariableReplacer(sc).substitute(subjectOfReplacement, new SubstitutionContext(hsRequest, null, null, null), new ChainedSubstitutionFilters(Collections.EMPTY_LIST));
    }
    
    public String substitute(String subjectOfReplacement, SubstitutionContext ctx,
                             SubstitutionFilterChain nextFilter) {
        Matcher varMatcher = toVariablePattern.matcher(subjectOfReplacement);
        StringBuffer sb = new StringBuffer();
        boolean anyMatches = false;

        int lastAppendPosition = 0;
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
                varValue = varReplace(varStr, ctx.getHsRequest());
                if (log.isDebugEnabled()) log.debug("resolved to: " + varValue);
            } else {
                if (log.isDebugEnabled()) log.debug("variable reference is null " + varMatcher);
            }
            String stringBeforeMatch = subjectOfReplacement.substring(lastAppendPosition, varMatcher.start());
            sb.append(nextFilter.substitute(stringBeforeMatch, ctx));
            sb.append(varValue);
            lastAppendPosition = varMatcher.end();
        }
        if (anyMatches) {
            String stringAfterMatch = subjectOfReplacement.substring(lastAppendPosition);
            sb.append(nextFilter.substitute(stringAfterMatch, ctx));
            log.debug("replaced sb is " + sb);
            return sb.toString();
        }
        return nextFilter.substitute(subjectOfReplacement, ctx);
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
                HttpSession session = hsRequest.getSession(false);
                if (session != null && varSubName != null) {
                    sessionAttributeValue = session.getAttribute(varSubName);
                }
                return attributeVariable(sessionAttributeValue, varSubName);

            case TypeConverter.TYPE_SESSION_IS_NEW:
                boolean sessionNew = false;
                HttpSession sessionIsNew = hsRequest.getSession(false);
                if (sessionIsNew != null) {
                    sessionNew = sessionIsNew.isNew();
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
                return e.getClass().getName();

            case TypeConverter.TYPE_HEADER:
                return StringUtils.notNull(hsRequest.getHeader(varSubName));

            case TypeConverter.TYPE_SERVLET_CONTEXT:
            	//ServletContext servletContext = (hsRequest.getSession(true).getServletContext());   
			Object attr = servletContext.getAttribute(varSubName);
			if (attr == null) {
				log.debug("No context attribute " + varSubName
						+ ", must be an init-param");
				return servletContext.getInitParameter(varSubName);
			} else {
				return StringUtils.notNull(attr.toString());
			}
                
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
