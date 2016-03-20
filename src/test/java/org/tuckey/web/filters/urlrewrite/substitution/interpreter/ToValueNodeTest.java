package org.tuckey.web.filters.urlrewrite.substitution.interpreter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Assert;
import org.junit.Test;
import org.tuckey.web.filters.urlrewrite.substitution.ChainedSubstitutionFilters;

import java.util.Collections;

public class ToValueNodeTest {

    @Test
    public void test() throws ParseException {
        Context context = new Context("${replace:my cat is a blue cat:cat:dog}");
        ToValueNode node = new ToValueNode(null, new ChainedSubstitutionFilters(Collections.emptyList()));
        node.parse(context);
        ElementNode target = (ElementNode) node.elementList.get(0);
        Assert.assertTrue(target.node instanceof FunctionNode);
        assertThat(node.evaluate(), is("my dog is a blue dog"));
    }

    @Test
    public void test2() throws ParseException {
        Context context = new Context("/search/${trim: abc ${lower:Hello World} }/hoge/");
        ToValueNode node = new ToValueNode(null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST));
        node.parse(context);
        assertThat(node.evaluate(), is("/search/abc hello world/hoge/"));
    }

    @Test
    public void test3() throws ParseException {
        Context context = new Context("/search/${lower:${upper:ABCD}}");
        ToValueNode node = new ToValueNode(null, new ChainedSubstitutionFilters(Collections.EMPTY_LIST));
        node.parse(context);
        assertThat(node.evaluate(), is("/search/abcd"));
    }
}
