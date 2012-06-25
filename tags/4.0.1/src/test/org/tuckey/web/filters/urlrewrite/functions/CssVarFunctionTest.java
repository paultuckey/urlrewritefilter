package org.tuckey.web.filters.urlrewrite.functions;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;


public class CssVarFunctionTest extends TestCase {

    String nl = System.getProperty("line.separator");

    String css = "" +
            "@variables {" + nl +
            "    gadgetBodyLinkColor: #c0c0c0;" + nl +
            "}" + nl +
            "" + nl +
            "h1 {" + nl +
            "    border-color: var(gadgetBodyLinkColor);" + nl +
            "}";

    String css2 = "" +
            "@variables {" + nl +
            "    gadgetBodyLinkColor: #c0c0c0;" + nl +
            "    gadgetBodyLinkColor2: #f0c0c0;" + nl +
            "}" + nl +
            "" + nl +
            "h1 {" + nl +
            "    color: var(gadgetBodyLinkColor2);" + nl +
            "    border-color: var(gadgetBodyLinkColor);" + nl +
            "}";


    public void testSimple() {
        assertEquals(nl +
                "h1 {" + nl +
                "    border-color:  #c0c0c0;" + nl +
                "}",
                CssVarFunction.parse(css, null));
    }

    public void testSimpleMap() {
        Map in = new HashMap();
        in.put("gadgetBodyLinkColor", "blue");
        assertEquals(nl +
                "h1 {" + nl +
                "    border-color: blue;" + nl +
                "}",
                CssVarFunction.parse(css, in));
    }

    public void testSimple2() {
        assertEquals(nl +
                "h1 {" + nl +
                "    color:  #f0c0c0;" + nl +
                "    border-color:  #c0c0c0;" + nl +
                "}",
                CssVarFunction.parse(css2, null));
    }


}
