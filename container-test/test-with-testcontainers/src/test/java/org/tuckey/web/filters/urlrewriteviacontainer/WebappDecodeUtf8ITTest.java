package org.tuckey.web.filters.urlrewriteviacontainer;


import org.apache.commons.httpclient.methods.GetMethod;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * todo: need to do a few tests
 * <p/>
 * with eocode-using not set (ie, browser encoding used, step down to utf8)
 * with eocode-using set to utf (force always with a specific decoding)
 * with eocode-using not set to null (never decode)
 * accept-encoding header?
 * <p/>
 * <p/>
 * don't decode anything - null
 * browser then utf then default - default browser,utf
 * browser then don't decode - default browser,null
 * always utf - utf
 * <p/>
 * <p/>
 * options: browser (may fail), enc (unlikely fail)
 */
public class WebappDecodeUtf8ITTest extends ContainerTestBase {

    protected String getApp() {
        return "webapp";
    }

    protected String getConf() {
        return "urlrewrite-decode-utf8.xml";
    }

    @BeforeEach
    public void beforeEach() throws Exception {
        super.setUp();
        super.recordRewriteStatus();
    }

    /**
     *
     */
    @Test
    public void testTestUtf() throws ServletException, IOException {
        String utfSampleString = "m\u0101ori";
        GetMethod method = new GetMethod(getBaseUrl() + "/utf/" + URLEncoder.encode(utfSampleString, "UTF8") + "/");
        method.setRequestHeader("Accept-Encoding", "utf8");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals("/" + getApp() + "/utf-redir/done/", method.getResponseHeader("Location").getValue());
    }


}
