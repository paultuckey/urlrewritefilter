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

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.substitution.ChainedSubstitutionFilters;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionContext;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionFilterChain;

/**
 * @author Paul Tuckey
 * @version $Revision: 36 $ $Date: 2006-09-19 18:32:39 +1200 (Tue, 19 Sep 2006) $
 */
public class StringMatchingMatcherTest extends TestCase {


    public static String replaceAll(StringMatchingMatcher matcher, String from, String replacement) {
        SubstitutionContext substitutionContext = new SubstitutionContext(null, matcher, null, replacement);
        SubstitutionFilterChain substitutionFilter = ChainedSubstitutionFilters.getDefaultSubstitutionChain(true, false, false, false);
        return substitutionFilter.substitute(from, substitutionContext);
    }

    public void testPatterns() throws StringMatchingPatternSyntaxException {
        RegexPattern pat1 = new RegexPattern("a(a)", false);
        StringMatchingMatcher mat1 = pat1.matcher("aaf");
        assertEquals("$1f", replaceAll(mat1, "aaf", "\\$1"));

        RegexPattern pat2 = new RegexPattern("aa", false);
        StringMatchingMatcher mat2 = pat2.matcher("aaf");
        assertEquals("$1f", replaceAll(mat2, "aaf", "\\$1"));

        WildcardPattern pat3 = new WildcardPattern("/*/*/*/");
        StringMatchingMatcher mat3 = pat3.matcher("/aa/bb/cc/");
        assertTrue(mat3.find());
        assertEquals(3, mat3.groupCount());
        assertEquals("aabbcc$3$2", replaceAll(mat3, "/aa/bb/cc/", "$1$2$3\\$3\\$2"));

        // try again with no matches
        WildcardPattern pat4 = new WildcardPattern("aa");
        StringMatchingMatcher mat4 = pat4.matcher("aaf");
        assertEquals("aaf", replaceAll(mat4, "aaf", "a\\$1b"));

        WildcardPattern pat5 = new WildcardPattern("/**");
        StringMatchingMatcher mat5 = pat5.matcher("/aa/bb/cc/?aa&bb#cc");
        assertTrue(mat5.find());
        assertEquals(1, mat5.groupCount());
        assertEquals("aa/bb/cc/?aa&bb#cc$3$2", replaceAll(mat5, "/aa/bb/cc/?aa&bb#cc", "$1$2$3\\$3\\$2"));

        WildcardPattern pat6 = new WildcardPattern("/aa/**");
        StringMatchingMatcher mat6 = pat6.matcher("/aa/bb/cc/?aa&bb#cc");
        assertTrue(mat6.find());
        assertEquals(1, mat6.groupCount());
        assertEquals("bb/cc/?aa&bb#cc$3$2", replaceAll(mat6, "/aa/bb/cc/?aa&bb#cc", "$1$2$3\\$3\\$2"));

        WildcardPattern pat7 = new WildcardPattern("/ee/**");
        StringMatchingMatcher mat7 = pat7.matcher("/aa/bb/cc/?aa&bb#cc");
        assertFalse(mat7.find());

    }

    /**
     * this was throwing an exception... it shouldn't.
     */
    public void testStrangeProblem1() throws StringMatchingPatternSyntaxException {
        RegexPattern pat = new RegexPattern("^(/.*)$", false);
        StringMatchingMatcher mat = pat.matcher("/tester/one-level-sub/two-leel-sub/");
        mat.find();
        mat.groupCount();
        mat.groupCount();
        mat.groupCount();
        mat.group(1);

    }

}
