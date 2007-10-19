package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.Condition;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.NormalRule;

public class ModRewriteConfLoaderTest extends TestCase {

    ModRewriteConfLoader loader = new ModRewriteConfLoader();

    public void setUp() {
        Log.setLevel("DEBUG");
    }

    public void testEngine() {
        Conf conf = loader.process("RewriteEngine on");
        assertTrue(conf.isEngineEnabled());
    }

    public void testEngine2() {
        Conf conf = loader.process("RewriteEngine off");
        assertFalse(conf.isEngineEnabled());
    }

    public void testSimple2() {
        Conf conf = loader.process("\n" +
                "    # redirect mozilla to another area                         \n" +
                "    RewriteCond  %{HTTP_USER_AGENT}  ^Mozilla.*                \n" +
                "    RewriteRule  ^/$                 /homepage.max.html  [L]   ");
        assertEquals(1, conf.getRules().size());
        assertEquals(1, ((NormalRule) conf.getRules().get(0)).getConditions().size());
        assertEquals("header", ((Condition) ((NormalRule) conf.getRules().get(0)).getConditions().get(0)).getType());
        assertEquals("user-agent", ((Condition) ((NormalRule) conf.getRules().get(0)).getConditions().get(0)).getName());
        assertEquals("^Mozilla.*", ((Condition) ((NormalRule) conf.getRules().get(0)).getConditions().get(0)).getValue());
        assertEquals("redirect mozilla to another area", ((NormalRule) conf.getRules().get(0)).getNote());
        assertEquals("^/$", ((NormalRule) conf.getRules().get(0)).getFrom());
        assertEquals("/homepage.max.html", ((NormalRule) conf.getRules().get(0)).getTo());
        assertEquals(true, ((NormalRule) conf.getRules().get(0)).isLast());
    }


}
