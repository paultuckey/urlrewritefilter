package org.tuckey.web.filters.urlrewrite.substitution.interpreter;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ContextTest {

    @Test
    public void testContext() {
        Context target = new Context("${escapePath:UTF-8:a b c : other / path}");
        assertThat(target.currentToken(), is("${"));
        assertThat(target.nextToken(), is("escapePath"));

        assertThat(target.nextToken(), is(":"));
        assertThat(target.nextToken(), is("UTF-8"));
        assertThat(target.nextToken(), is(":"));
        assertThat(target.nextToken(), is("a b c "));
        assertThat(target.nextToken(), is(":"));
        assertThat(target.nextToken(), is(" other / path"));
        assertThat(target.nextToken(), is("}"));
        assertThat(target.nextToken(), nullValue());
    }

    @Test
    public void testSkipToken() throws Exception {
        Context target = new Context("${escapePath:UTF-8:a b c : other / path}");
        target.skipToken("${");
        assertThat(target.currentToken(), is("escapePath"));
        assertThat(target.nextToken(), is(":"));
    }

}
