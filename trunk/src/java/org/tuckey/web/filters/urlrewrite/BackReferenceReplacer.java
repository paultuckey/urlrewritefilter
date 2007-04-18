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
import org.tuckey.web.filters.urlrewrite.utils.StringMatchingMatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class BackReferenceReplacer {

    private static Log log = Log.getLog(BackReferenceReplacer.class);

    private static Pattern backRefPattern = Pattern.compile("(?<!\\\\)%([0-9])");
    private static Pattern escapedBackRefPattern = Pattern.compile("\\\\(%[0-9])");

    /**
     * look for back reference a % followed by a number
     */
    public static boolean containsBackRef(String to) {
        Matcher backRefMatcher = backRefPattern.matcher(to);
        return backRefMatcher.find();
    }


    public static String replace(ConditionMatch lastConditionMatch, String subjectOfReplacement) {
        if (lastConditionMatch == null) {
            return subjectOfReplacement;
        }

        StringMatchingMatcher lastConditionMatchMatcher = lastConditionMatch.getMatcher();

        if (lastConditionMatchMatcher != null) {
            int lastCondMatcherGroupCount = lastConditionMatchMatcher.groupCount();
            if (lastCondMatcherGroupCount > 0) {

                Matcher backRefMatcher = backRefPattern.matcher(subjectOfReplacement);

                StringBuffer sb = new StringBuffer();
                boolean anyMatches = false;

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
                    backRefMatcher.appendReplacement(sb, conditionMatch);
                }
                if (anyMatches) {
                    backRefMatcher.appendTail(sb);
                    if (log.isDebugEnabled()) log.debug("replaced sb is " + sb);
                    subjectOfReplacement = sb.toString();
                }
            }
        }

        Matcher escapedVariableMatcher = escapedBackRefPattern.matcher(subjectOfReplacement);
        subjectOfReplacement = escapedVariableMatcher.replaceAll("$1");

        return subjectOfReplacement;
    }
}
