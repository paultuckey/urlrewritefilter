package org.tuckey.web.filters.urlrewrite.substitution.interpreter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tuckey.web.filters.urlrewrite.substitution.ChainedSubstitutionFilters;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

public class FunctionTest {

    @Test
    public void testTolower() {
        assertThat(Function.TOLOWER.execute("ABCD", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("abcd"));
        assertThat(Function.TOLOWER.execute("abCD1234XYzw", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("abcd1234xyzw"));
    }

    @Test
    public void testToupper() {
        assertThat(Function.TOUPPER.execute("abcd", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("ABCD"));
        assertThat(Function.TOUPPER.execute("abCD1234XYzw", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("ABCD1234XYZW"));
    }

    @Test
    public void testTrim() {
        assertThat(Function.TRIM.execute("   abcd  ", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("abcd"));
        assertThat(Function.TRIM.execute("    ", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is(""));
    }

    @Test
    public void testReplaceall() {
        assertThat(Function.REPLACEALL.execute("abcd1234abcxyzw:abc:stu", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("stud1234stuxyzw"));
    }

    @Test
    public void testReplacefirst() {
        assertThat(Function.REPLACEFIRST.execute("abcd1234abcxyzw:abc:stu", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("stud1234abcxyzw"));
    }

    @Test
    public void testEscape() throws UnsupportedEncodingException {
        // \u65E5\u672C\u8A9E is some Japanese string. assertThat(Function.ESCAPE.execute("utf-8:abc", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("abc"));
        assertThat(Function.ESCAPE.execute("utf8:\u65E5\u672C\u8A9E", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("%E6%97%A5%E6%9C%AC%E8%AA%9E"));
        assertThat(Function.ESCAPE.execute("utf8:abcd!&'()0#$%&", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("abcd%21%26%27%28%290%23%24%25%26"));
    }

    @Test
    public void testEscapepath() throws UnsupportedEncodingException {
        /*
         * I could't know this test is enough to keep the quality. Is there any specification or
         * document for this method??? TODO check true spec of this method and do test properly
         */
        assertThat(Function.ESCAPEPATH.execute("utf8:\u65E5\u672C\u8A9E", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("%e6%97%a5%e6%9c%ac%e8%aa%9e"));
    }

    @Test
    public void testUnescape() throws UnsupportedEncodingException {
        assertThat(Function.UNESCAPE.execute("utf-8:abc", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("abc"));
        assertThat(
                Function.UNESCAPE.execute("utf8:%E6%97%A5%E6%9C%AC%E8%AA%9E", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("\u65E5\u672C\u8A9E"));
        assertThat(Function.UNESCAPE.execute("utf8:abcd%21%26%27%28%290%23%24%25%26", null,
                new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("abcd!&'()0#$%&"));
    }

    @Test
    public void testUnescapepath() throws UnsupportedEncodingException {
        /*
         * I am not sure that this test is enough to keep the quality. 
         * Is there any specification or document for this method?
         */
        assertThat(Function.UNESCAPEPATH.execute("utf8:\u65E5\u672C\u8A9E", null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST)),
                is("???"));
        assertThat(Function.UNESCAPEPATH.execute("utf8:%e6%97%a5%e6%9c%ac%e8%aa%9e", null,
                new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("\u65E5\u672C\u8A9E"));
        assertThat(Function.UNESCAPEPATH.execute("utf8:abcd%21%26%27%28%290%23%24%25%26", null,
                new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("abcd!&'()0#$%&"));
        assertThat(Function.UNESCAPEPATH.execute("utf8:abCD%21%26%27%28%290%23%24%25%26", null,
                new ChainedSubstitutionFilters(Collections.EMPTY_LIST)), is("abCD!&'()0#$%&"));
    }
}
