package org.tuckey.web.filters.urlrewrite.substitution.interpreter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tuckey.web.filters.urlrewrite.substitution.ChainedSubstitutionFilters;

import java.util.Collections;

public class FunctionNodeTest {

    @Test
    public void test() throws ParseException {
        Context context = new Context("${trim: abc ${lower:Hello World} }");
        FunctionNode node = new FunctionNode(null, new ChainedSubstitutionFilters(Collections.emptyList()));
        node.parse(context);
        assertThat(node.evaluate(), is("abc hello world"));
    }
}
