package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;

/**
 * @author Paul Tuckey
 * @version $Revision: 36 $ $Date: 2006-09-19 18:32:39 +1200 (Tue, 19 Sep 2006) $
 */
public class StringMatchingMatcherTest extends TestCase {


    public void testEscaped() throws StringMatchingPatternSyntaxException {
        RegexPattern pat1 = new RegexPattern("a(a)", false);
        StringMatchingMatcher mat1 = pat1.matcher("aaf");
        assertEquals("$1f", mat1.replaceAll("\\$1"));

        RegexPattern pat2 = new RegexPattern("aa", false);
        StringMatchingMatcher mat2 = pat2.matcher("aaf");
        assertEquals("$1f", mat2.replaceAll("\\$1"));

        WildcardPattern pat3 = new WildcardPattern("/*/*/*/");
        StringMatchingMatcher mat3 = pat3.matcher("/aa/bb/cc/");
        assertTrue(mat3.find());
        assertEquals(3, mat3.groupCount());
        assertEquals("aabbcc$3$2", mat3.replaceAll("$1$2$3\\$3\\$2"));

        // try again with no matches
        WildcardPattern pat4 = new WildcardPattern("aa");
        StringMatchingMatcher mat4 = pat4.matcher("aaf");
        assertEquals("a$1b", mat4.replaceAll("a\\$1b"));
    }

    /**
     * this was throwing an exception... it shouldn't.
     */
    public void testStrangeProblem1() throws StringMatchingPatternSyntaxException {
        RegexPattern pat = new RegexPattern("^(/.*)$", false);
        StringMatchingMatcher mat = pat.matcher("/tester/one-level-sub/two-leel-sub/");
        mat.find();
        mat.replaceAll("$1");
        mat.groupCount();
        mat.groupCount();
        mat.groupCount();
        mat.group(1);

    }

}
