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
package org.tuckey.web.filters.urlrewrite.utils;

import org.tuckey.web.filters.urlrewrite.VariableReplacer;
import org.tuckey.web.filters.urlrewrite.functions.StringFunctions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for function replacement.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class FunctionReplacer {

    private static Log log = Log.getLog(VariableReplacer.class);

    private static Pattern functionPattern = Pattern.compile("(?<!\\\\)\\$\\{(.*?)\\}");

    public static boolean containsFunction(String to) {
        Matcher functionMatcher = functionPattern.matcher(to);
        return functionMatcher.find();
    }

    public static String replace(String subjectOfReplacement) {
        Matcher functionMatcher = functionPattern.matcher(subjectOfReplacement);
        StringBuffer sb = new StringBuffer();
        boolean anyMatches = false;

        while (functionMatcher.find()) {
            anyMatches = true;
            int groupCount = functionMatcher.groupCount();
            if (groupCount < 1) {
                log.error("group count on function finder regex is not as expected");
                if (log.isDebugEnabled()) {
                    log.error("functionMatcher: " + functionMatcher.toString());
                }
                continue;
            }
            String varStr = functionMatcher.group(1);
            String varValue = "";
            if (varStr != null) {
                varValue = functionReplace(varStr);
                if (log.isDebugEnabled()) log.debug("resolved to: " + varValue);
            } else {
                if (log.isDebugEnabled()) log.debug("variable reference is null " + functionMatcher);
            }
            functionMatcher.appendReplacement(sb, varValue);
        }
        if (anyMatches) {
            functionMatcher.appendTail(sb);
            log.debug("replaced sb is " + sb);
            return sb.toString();
        }
        return subjectOfReplacement;
    }


    /**
     * Handles the fetching of the variable value from the request.
     */
    private static String functionReplace(String originalVarStr) {
        // get the sub name if any ie for headers etc header:user-agent
        String varSubName = null;
        String varType;
        int colonIdx = originalVarStr.indexOf(":");
        if (colonIdx != -1 && colonIdx + 1 < originalVarStr.length()) {
            varSubName = originalVarStr.substring(colonIdx + 1);
            varType = originalVarStr.substring(0, colonIdx);
            if (log.isDebugEnabled()) log.debug("function ${" + originalVarStr + "} type: " + varType +
                    ", name: '" + varSubName + "'");
        } else {
            varType = originalVarStr;
            if (log.isDebugEnabled()) log.debug("function ${" + originalVarStr + "} type: " + varType);
        }
        String functionResult = "";
        // check for some built in functions
        if ("replace".equalsIgnoreCase(varType) || "replaceAll".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.replaceAll(varSubName);
        } else if ("replaceFirst".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.replaceFirst(varSubName);
        } else if ("escape".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.escape(varSubName);
        } else if ("unescape".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.unescape(varSubName);
        } else if ("lower".equalsIgnoreCase(varType) || "toLower".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.toLower(varSubName);
        } else if ("upper".equalsIgnoreCase(varType) || "toUpper".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.toUpper(varSubName);
        } else if ("trim".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.trim(varSubName);
        } else if ("length".equalsIgnoreCase(varType)) {
            functionResult = StringFunctions.length(varSubName);
        } else {
            log.error("function ${" + originalVarStr + "} type '" + varType + "' not a valid type");
        }
        return functionResult;
    }


}