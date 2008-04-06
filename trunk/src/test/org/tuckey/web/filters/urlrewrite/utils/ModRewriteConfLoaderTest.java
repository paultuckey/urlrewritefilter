package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.Condition;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.NormalRule;

import java.io.IOException;
import java.io.InputStream;

public class ModRewriteConfLoaderTest extends TestCase {

    ModRewriteConfLoader loader = new ModRewriteConfLoader();
    Conf conf;

    public static final String BASE_PATH = "/org/tuckey/web/filters/urlrewrite/utils/";

    public void setUp() {
        Log.setLevel("DEBUG");
        conf = new Conf();
    }

    public void testEngine() {
        loader.process("RewriteEngine on", conf);
        assertTrue(conf.isEngineEnabled());
    }

    public void testEngine2() {
        loader.process("RewriteEngine off", conf);
        assertFalse(conf.isEngineEnabled());
    }

    public void testLoadFromFile() throws IOException {
        InputStream is = ModRewriteConfLoaderTest.class.getResourceAsStream(BASE_PATH + "htaccess-test1.txt");
        loader.process(is, conf);
        assertTrue(conf.isEngineEnabled());
        assertEquals(1, conf.getRules().size());
    }

    public void testSimple2() {
        loader.process("\n" +
                "    # redirect mozilla to another area                         \n" +
                "    RewriteCond  %{HTTP_USER_AGENT}  ^Mozilla.*                \n" +
                "    RewriteRule  ^/$                 /homepage.max.html  [L]   ", conf);
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
