package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Paul Tuckey
 * @version $Revision: 40 $ $Date: 2006-10-27 15:12:37 +1300 (Fri, 27 Oct 2006) $
 */
public class RequestProxyTest extends TestCase {

    MockResponse response;
    MockRequest request;

    public void setUp() {
        Log.setLevel("DEBUG");
        response = new MockResponse();
        request = new MockRequest();
    }

    public void testUseProxyServer() throws IOException, ServletException {
        assertEquals(3128, RequestProxy.getUseProxyServer("myproxyserver:3128").getPort());
        assertEquals(80, RequestProxy.getUseProxyServer("myproxyserver:A3128").getPort());
        assertEquals("myproxyserver", RequestProxy.getUseProxyServer("myproxyserver:A3128").getHostName());
    }


    public void testProxyRule() throws IOException, ServletException, InvocationTargetException {
        Conf conf = new Conf();
        NormalRule rule = new NormalRule();
        rule.setFrom("/path/(.*)");
        rule.setTo("http://tuckey.org/urlrewrite/$1");
        rule.setToType("proxy");
        conf.addRule(rule);
        conf.initialise();

        MockRequest request = new MockRequest("/path/index.html?id=46");
        //UrlRewriteWrappedResponse urlRewriteWrappedResponse = new UrlRewriteWrappedResponse(response, request, urlRewriter);

        NormalRewrittenUrl rewrittenUrl = (NormalRewrittenUrl) rule.matches(request.getRequestURI(), request, response);

        assertEquals("proxy should be default type", "proxy", rule.getToType());
        assertEquals("http://tuckey.org/urlrewrite/index.html?id=46", rewrittenUrl.getTarget());

        // This work's
        // commented to avoid network access during unit tests
        //RequestProxy.execute(rewrittenUrl.getTarget(), request, response);
    }


}
