package org.tuckey.web.filters.urlrewriteviacontainer;


import org.apache.commons.httpclient.methods.GetMethod;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;

/**
 *
 */
public class WebappDecodeNoneTest extends ContainerTestBase {

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
        String encodedStr = URLEncoder.encode("Fêtel'haïvolapük", "UTF8");
        GetMethod method = new GetMethod(getBaseUrl() + "/utf/" + encodedStr + "/");
        method.setRequestHeader("Accept-Encoding", "utf8");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/utf-redir/done/" + encodedStr + "/", method.getResponseHeader("Location").getValue());
    }

    public void testTestUtfToBrowser() throws ServletException, IOException {
        if ( "orion2.0.5".equals(getContainerId())) return; // orion not supported
        String encodedStr = URLEncoder.encode("Fêtel'haïvolapük", "UTF8");
        GetMethod method = new GetMethod(getBaseUrl() + "/utf/" + encodedStr + "/to-browser/");
        method.setRequestHeader("Accept-Encoding", "utf8");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/utf-redir/done/" + encodedStr + "/to-browser/", method.getResponseHeader("Location").getValue());
    }


}