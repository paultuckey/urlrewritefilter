package org.tuckey.web.filters.urlrewrite.utils;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.Run;


public class JsonSerialiserTest extends TestCase {

    public void testJson1() {
        assertEquals("{\"newEachTime\":false,\"javaClass\":\"org.tuckey.web.filters.urlrewrite.Run\"," +
                "\"handler\":\"standard\",\"methodSignature\":\"run\",\"methodStr\":\"run\",\"valid\":false," +
                "\"error\":null,\"id\":0,\"filter\":false,\"initialised\":false,\"classStr\":null," +
                "\"runClassInstance\":null,\"displayName\":\"Run 0\"}",
                JsonSerialiser.toJsonString(new Run()));
    }

}
