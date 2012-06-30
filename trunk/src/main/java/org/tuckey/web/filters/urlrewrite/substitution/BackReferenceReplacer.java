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

import org.tuckey.web.filters.urlrewrite.ConditionMatch;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.StringMatchingMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Back references (eg, %1, %2 etc) replacer.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class BackReferenceReplacer implements SubstitutionFilter {

    private static Log log = Log.getLog(BackReferenceReplacer.class);

    private static Pattern backRefPattern = Pattern.compile("(?<!\\\\)%([0-9])");

    /**
     * look for back reference a % followed by a number
     */
    public static boolean containsBackRef(String to) {
        Matcher backRefMatcher = backRefPattern.matcher(to);
        return backRefMatcher.find();
    }


    public String substitute(String subjectOfReplacement, SubstitutionContext ctx,
                             SubstitutionFilterChain nextFilter) {
        ConditionMatch lastConditionMatch = ctx.getLastConditionMatch();

        if (lastConditionMatch == null) {
            return nextFilter.substitute(subjectOfReplacement, ctx);
        }

        StringMatchingMatcher lastConditionMatchMatcher = lastConditionMatch.getMatcher();

        if (lastConditionMatchMatcher != null) {
            int lastCondMatcherGroupCount = lastConditionMatchMatcher.groupCount();
            if (lastCondMatcherGroupCount > 0) {

                Matcher backRefMatcher = backRefPattern.matcher(subjectOfReplacement);

                StringBuffer sb = new StringBuffer();
                boolean anyMatches = false;
                int lastAppendPosition = 0;
                while (backRefMatcher.find()) {
                    anyMatches = true;
                    int groupCount = backRefMatcher.groupCount();
                    if (groupCount < 1) {
                        log.error("group count on backref finder regex is not as expected");
                        if (log.isDebugEnabled()) {
                            log.error("backRefMatcher: " + backRefMatcher.toString());
                        }
                        continue;
                    }
                    String varStr = backRefMatcher.group(1);

                    boolean validBackref = false;
                    int varInt = 0;
                    log.debug("found " + varStr);
                    // now grab this match from lastConditionMatchMatcher
                    try {
                        varInt = Integer.parseInt(varStr);
                        if (varInt > lastCondMatcherGroupCount) {
                            log.error("backref %" + varInt + " not found in conditon ");
                            if (log.isDebugEnabled()) {
                                log.debug("condition matcher: " + lastConditionMatchMatcher.toString());
                            }
                        } else {
                            validBackref = true;
                        }
                    } catch (NumberFormatException nfe) {
                        log.error("could not parse backref " + varStr + " to number");
                    }
                    String conditionMatch = "";
                    if (validBackref) {
                        conditionMatch = lastConditionMatchMatcher.group(varInt);
                    }
                    String stringBeforeMatch = subjectOfReplacement.substring(lastAppendPosition, backRefMatcher.start());
                    sb.append(nextFilter.substitute(stringBeforeMatch, ctx));
                    sb.append(conditionMatch);
                    lastAppendPosition = backRefMatcher.end();
                }
                if (anyMatches) {
                    String stringAfterMatch = subjectOfReplacement.substring(lastAppendPosition);
                    sb.append(nextFilter.substitute(stringAfterMatch, ctx));
                    if (log.isDebugEnabled()) log.debug("replaced sb is " + sb);
                    return sb.toString();
                }
            }
        }

        return nextFilter.substitute(subjectOfReplacement, ctx);
    }


}
