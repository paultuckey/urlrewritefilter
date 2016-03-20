package org.tuckey.web.filters.urlrewrite.substitution.interpreter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TokenizerTest {

    @Test
    public void testTokenizer() {
        doTokenSizeTest("${escapePath:UTF-8:a b c : other / path}", 9);
    }

    @Test
    public void testTokenizer2() {
        doTokenSizeTest("abcd${escapePath:UTF-8:a b c : other / path}", 10);
    }

    @Test
    public void testTokenizer3() {
        doTokenSizeTest("/test/${upper:$1}/${upper:$1}.html", 13);
    }

    @Test
    public void testTokenizer4() {
        doTokenSizeTest("/search/${escapePath:${unescape:$1}}", 10);
    }

    @Test
    public void testTokenizer5() {
        doTokenSizeTest("/search/${escapePath:${unescape:$1}}/hoge/${unescape:$1}", 16);
    }

    @Test
    public void testTokenizer6() {
        doTokenSizeTest("/search/${escapePath:${unescape:$1}}/hoge/${escapePath:${unescape:$1}}", 20);
    }

    @Test
    public void testTokenizer7() {
        doTokenSizeTest("/search/${escapePath:${unescape:$1}}/hoge/${escapePath:${unescape:$1}}abced:hoge", 23);
    }

    @Test
    public void testTokenizer8() {
        doTokenSizeTest("/search/${trim: abc ${lower:Hello World} }/hoge/", 13);
    }

    @Test
    public void testTokenizer9() {
        doTokenSizeTest("/file/hogehoge?fuga=$1&amp;type=${escape:$2}&amp;url=%{request-url}hogehoge&amp;", 8);
        doTokenSizeTest("${escape:$2}&amp;url=%{request-url}", 6);
        doTokenSizeTest("url=%{request-url}${escape:$2}", 6);
        doTokenSizeTest("url=%{request-url}${escape:%{request-url}}", 6);
        doTokenSizeTest("url=%{request-url}${escape:%{request-url}}%{request-url}", 7);
    }

    private void doTokenSizeTest(String pattern, int size) {
        Tokenizer target = new Tokenizer(pattern);
        assertEquals(size, target.tokens.size());

    }
}
