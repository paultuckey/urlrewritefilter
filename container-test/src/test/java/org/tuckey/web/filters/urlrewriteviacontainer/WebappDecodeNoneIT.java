package org.tuckey.web.filters.urlrewriteviacontainer;


import org.apache.commons.httpclient.methods.GetMethod;

import jakarta.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;

/**
 *
 */
public class WebappDecodeNoneIT extends ContainerTestBase {

    protected String getApp() {
        return "webapp";
    }

    protected String getConf() {
        return "urlrewrite-decode-none.xml";
    }

    public void testSetup() throws IOException {
        super.recordRewriteStatus();
    }


    /**
     * note, had trouble keeping true utf (multi byte) chars as cvs buggers them up!
     */
    public void testTestUtf() throws ServletException, IOException {
        if ( "orion2.0.5".equals(getContainerId())) return; // orion not supported
        String encodedStr = URLEncoder.encode("m\u0101ori", "UTF8");
        GetMethod method = new GetMethod(getBaseUrl() + "/utf/" + encodedStr + "/");
        method.setRequestHeader("Accept-Encoding", "utf8");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertNotNull("no location header", method.getResponseHeader("Location"));
        assertEquals(getBaseUrl() + "/utf-redir/done/" + encodedStr + "/", method.getResponseHeader("Location").getValue());
    }

    public void testNoDecode() throws IOException {
        if ( "orion2.0.5".equals(getContainerId())) return; // jsp's with % in path not supported
        if ( "tomcat-4.1.31".equals(getContainerId())) return; // jsp's with % in path not supported

        GetMethod method = new GetMethod(getBaseUrl() + "/no-decode-test/D%25%2cD");
        client.executeMethod(method);
        assertEquals("this is no-decode-test target jsp", method.getResponseBodyAsString());
    }

    public void testQueryStringNoDecode() throws IOException {
        if ( "orion2.0.5".equals(getContainerId())) return; // orion cannot correctly encode & into %26

        GetMethod method = new GetMethod(getBaseUrl() + "/query-string-no-decode/jack+%26+jones");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("http://query-string-no-decode-result.com/?q=jack+%26+jones&another=jack & jones", method.getResponseHeader("Location").getValue());
    }


}