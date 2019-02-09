package org.tuckey.web.filters.urlrewrite.substitution.interpreter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.tuckey.web.filters.urlrewrite.substitution.ChainedSubstitutionFilters;

import java.util.Collections;

public class ParamNodeTest {

    @Test
    public void test() throws ParseException {
        Context context = new Context("abcd:hogehoge:wawawa");
        ParamNode node = new ParamNode(null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST));
        node.parse(context);
        assertThat(node.elementList.get(0).toString(), is("abcd"));
        assertThat(node.secondElement.toString(), is("hogehoge:wawawa"));
    }

}
