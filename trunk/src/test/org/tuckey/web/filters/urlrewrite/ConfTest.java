/**
 * Copyright (c) 2005, Paul Tuckey
 * All rights reserved.
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;


/**
 * @author Paul Tuckey
 * @version $Revision: 44 $ $Date: 2006-11-02 12:29:14 +1300 (Thu, 02 Nov 2006) $
 */
public class ConfTest extends TestCase {

    public static final String BASE_XML_PATH = "/org/tuckey/web/filters/urlrewrite/";

    public void setUp() {
        Log.setLevel("DEBUG");
    }


    public void testGoodNormalConf() throws FileNotFoundException {
        InputStream is = ConfTest.class.getResourceAsStream(BASE_XML_PATH + "conf-test1.xml");
        Conf conf = new Conf(is, "conf-test1.xml");
        assertTrue(conf.isOk());
        assertEquals("regex", conf.getDefaultMatchType());
        assertEquals(false, conf.isUseContext());
        assertEquals(false, conf.isUseQueryString());
        assertEquals("utf-8", conf.getDecodeUsing());

        List rules = conf.getRules();
        List outboundRules = conf.getOutboundRules();
        List catches = conf.getCatchElems();

        NormalRule rule = (NormalRule) rules.get(0);
        assertEquals("basicfrom1", rule.getFrom());
        assertEquals("basicto1", rule.getTo());
        assertFalse("rule 1 last not loading correctly", rule.isLast());
        assertEquals("forward", rule.getToType());
        assertEquals(false, rule.isEncodeToUrl());
        SetAttribute set = (SetAttribute) rule.getSetAttributes().get(0);
        assertEquals("name of set, it's " + set.getName(), "valuenull", set.getName());
        assertNull("value of set should be null, it's " + set.getValue(), set.getValue());

        NormalRule rule1 = (NormalRule) rules.get(1);
        assertEquals("basicfrom2", rule1.getFrom());
        assertEquals("basicto2", rule1.getTo());
        assertTrue("rule 2 last not loading correctly", rule1.isLast());
        assertEquals("redirect", rule1.getToType());
        assertEquals("true", "" + rule1.isFromCaseSensitive());
        Condition cond = (Condition) rule1.getConditions().get(0);
        assertEquals("true", "" + cond.isCaseSensitive());
        assertEquals("greater", cond.getOperator());

        NormalRule rule2 = (NormalRule) rules.get(2);
        SetAttribute set2 = (SetAttribute) rule2.getSetAttributes().get(0);
        assertEquals("blah", set2.getName());
        assertEquals("$1", set2.getValue());

        ClassRule rule4 = (ClassRule) rules.get(4);
        assertEquals(true, rule4.isValid());

        ClassRule rule5 = (ClassRule) rules.get(5);
        assertEquals(true, rule5.isValid());

        NormalRule rule6 = (NormalRule) rules.get(6);
        Run run = (Run) rule6.getRuns().get(0);
        assertEquals("testValue", run.getInitParam("testName"));

        OutboundRule outboundRule = (OutboundRule) outboundRules.get(0);
        assertEquals("default encode on to test", outboundRule.getName());
        assertEquals(true, outboundRule.isEncodeToUrl());

        CatchElem catchElem = (CatchElem) catches.get(0);
        assertEquals(true, catchElem.isValid());
    }


    public void testNoDtdConf() throws FileNotFoundException {
        InputStream is = ConfTest.class.getResourceAsStream(BASE_XML_PATH + "conf-test-no-dtd.xml");
        Conf conf = new Conf(is, "conf-test-no-dtd.xml");
        assertTrue(conf.isOk());
        assertFalse(conf.isDecodeRequired());
    }

    public void testConfDefaults() throws FileNotFoundException {
        System.out.println("testConfDefaults");
        Conf conf = new Conf(ConfTest.class.getResource("conf-test2.xml"));
        assertTrue("Conf should have loaded ok", conf.isOk());
        assertEquals("use context should be true", true, conf.isUseContext());
        assertEquals("use query string should be true", true, conf.isUseQueryString());
        assertEquals("utf-16", conf.getDecodeUsing());
        assertEquals("wildcard", conf.getDefaultMatchType());
        assertEquals("wildcard", ((NormalRule) conf.getRules().get(0)).getMatchType());
        assertEquals("regex", ((NormalRule) conf.getRules().get(1)).getMatchType());
        assertEquals("wildcard", ((NormalRule) conf.getRules().get(2)).getMatchType());

        assertEquals("Included Rule (rule 3)", ((Rule) conf.getRules().get(3)).getDisplayName());
    }

    public void testConfBadParse() throws FileNotFoundException {
        Conf conf = new Conf(ConfTest.class.getResource("conf-test-bad-parse.xml"));
        assertFalse("a validation error should make the conf fail to load", conf.isOk());
    }

}

