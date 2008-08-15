package org.tuckey.web.filters.urlrewrite.container;

import org.apache.commons.httpclient.methods.GetMethod;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import java.io.IOException;


public class ModStyleHttpTest extends ContainerTestBase {

    public void testSimpleTest() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/simple/test");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("this is index.jsp", method.getResponseBodyAsString());
    }

    public void testStatus1() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/rewrite-status");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertTrue(method.getResponseBodyAsString().contains("Running Status"));
        assertFalse(method.getResponseBodyAsString().contains("Error"));
    }

    protected String getBaseUrl() {
        return super.getBaseUrl() + "/webapp-mod-style";
    }


}
