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

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tuckey.web.filters.urlrewrite.substitution.interpreter.Context;
import org.tuckey.web.filters.urlrewrite.substitution.interpreter.ParseException;
import org.tuckey.web.filters.urlrewrite.substitution.interpreter.ToValueNode;
import org.tuckey.web.filters.urlrewrite.utils.Log;

/**
 * Helper class for function replacement.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class FunctionReplacer implements SubstitutionFilter {

    private static Log log = Log.getLog(VariableReplacer.class);

    private static Pattern functionPattern = Pattern.compile("(?<!\\\\)\\$\\{(.*)\\}");

    public static boolean containsFunction(String to) {
        Matcher functionMatcher = functionPattern.matcher(to);
        return functionMatcher.find();
    }

    public static String replace(String subjectOfReplacement) {
        return new FunctionReplacer().substitute(subjectOfReplacement, null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST));
    }

    public String substitute(String subjectOfReplacement, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
        Context context = new Context(subjectOfReplacement);
        ToValueNode node = new ToValueNode(ctx, nextFilter);
        try {
            node.parse(context);
            return node.evaluate();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return subjectOfReplacement;
    }


}
