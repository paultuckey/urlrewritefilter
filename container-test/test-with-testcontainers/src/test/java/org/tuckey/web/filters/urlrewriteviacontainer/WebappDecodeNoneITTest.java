package org.tuckey.web.filters.urlrewriteviacontainer;


import org.apache.commons.httpclient.methods.GetMethod;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


import java.io.IOException;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class WebappDecodeNoneITTest extends ContainerTestBase {

    protected String getApp() {
        return "webapp";
    }

    protected String getConf() {
        return "urlrewrite-decode-none.xml";
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        super.setUp();
        super.recordRewriteStatus();
    }

    /**
     * note, had trouble keeping true utf (multi byte) chars as cvs buggers them up!
     */
    @Test
    public void testTestUtf() throws ServletException, IOException {
        if ( "orion2.0.5".equals(getContainerId())) return; // orion not supported
        String encodedStr = URLEncoder.encode("m\u0101ori", "UTF8");
        GetMethod method = new GetMethod(getBaseUrl() + "/utf/" + encodedStr + "/");
        method.setRequestHeader("Accept-Encoding", "utf8");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertNotNull(method.getResponseHeader("Location"), "no location header");
        assertEquals(getBaseUrl() + "/utf-redir/done/" + encodedStr + "/", method.getResponseHeader("Location").getValue());
    }

    @Test
    public void testNoDecode() throws IOException {
        if ( "orion2.0.5".equals(getContainerId())) return; // jsp's with % in path not supported
        if ( "tomcat-4.1.31".equals(getContainerId())) return; // jsp's with % in path not supported

        GetMethod method = new GetMethod(getBaseUrl() + "/no-decode-test/D%25%2cD");
        client.executeMethod(method);
        assertEquals("this is no-decode-test target jsp", method.getResponseBodyAsString());
    }

    @Test
    public void testQueryStringNoDecode() throws IOException {
        if ( "orion2.0.5".equals(getContainerId())) return; // orion cannot correctly encode & into %26

        GetMethod method = new GetMethod(getBaseUrl() + "/query-string-no-decode/jack+%26+jones");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("http://query-string-no-decode-result.com/?q=jack+%26+jones&another=jack & jones", method.getResponseHeader("Location").getValue());
    }


}