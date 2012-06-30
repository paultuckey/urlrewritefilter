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

import org.tuckey.web.filters.urlrewrite.utils.StringMatchingMatcher;

/**
 * Replaces each match of the condition to the given substituted pattern
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class PatternReplacer implements SubstitutionFilter {

    public String substitute(String from, SubstitutionContext ctx,
                             SubstitutionFilterChain nextFilter) {

        StringMatchingMatcher conditionMatcher = ctx.getMatcher();
        conditionMatcher.reset();
        StringBuffer sb = new StringBuffer();
        int lastMatchEnd = 0;
        while (conditionMatcher.find()) {
            // we do not substitute on the non-matched string since it is straight from the URL
            String notMatched = from.substring(lastMatchEnd, conditionMatcher.start());
            sb.append(notMatched);
            // we will replace the matched string with the appropriately expanded pattern
            String substitutedReplacement = nextFilter.substitute(ctx.getReplacePattern(), ctx);
            sb.append(substitutedReplacement);
            lastMatchEnd = conditionMatcher.end();
            // get out of there for wildcard patterns
            if (!conditionMatcher.isMultipleMatchingSupported())
                break;
        }
        // put the remaining ending non-matched string
        if (lastMatchEnd < from.length())
            sb.append(from.substring(lastMatchEnd));
        return sb.toString();
    }

}
