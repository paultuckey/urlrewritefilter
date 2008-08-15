package org.tuckey.web.filters.urlrewrite.container;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.GetMethod;
import org.tuckey.web.filters.urlrewrite.TestRunObj;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;


public class UrlRewriteFilterHttpTest extends ContainerTestBase {

    private String CONTENT = "<p>some content</p>";

    protected String getBaseUrl() {
        return super.getBaseUrl() + "/webapp";
    }


    public void testProduct() throws IOException, SAXException, InterruptedException {
        GetMethod method = new GetMethod(getBaseUrl() + "/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }


    public void testSimpleDistEx() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/test/status/");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/rewrite-status", method.getResponseHeader("Location").getValue());
    }

    public void testBasicSets() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/settest/674");
        client.executeMethod(method);
        assertNotNull(method.getResponseHeader("cache-control"));
        assertEquals("testsession: hello!", method.getResponseBodyAsString());
    }

    public void testMultipleProduct() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(getBaseUrl() + "/multiple/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }

    public void testNullTo() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/hideme/anb.jsp;dsaddd?asdasds#sdsfd");
        client.executeMethod(method);
        assertEquals("should have status set", 403, method.getStatusCode());
        assertFalse("should not output above content", CONTENT.equals(StringUtils.trim(method.getResponseBodyAsString())));
    }

    public void testYear() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/time/year/current");
        client.executeMethod(method);
        assertEquals("echo yearisbetween1970and3000", method.getResponseBodyAsString());
    }

    public void testTestAxis() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/services/blah?qwerty");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/axis/services/blah", method.getResponseHeader("Location").getValue());
    }

    public void testTestErik() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/eriktest/hi.ho");
        method.setFollowRedirects(false);
        method.addRequestHeader(new Header("host", "blah.com"));
        client.executeMethod(method);
        assertEquals("http://www.example.com/context/hi.ho", method.getResponseHeader("Location").getValue());
    }

    public void testTestEncode() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/went%20to%20bahamas/;jsessionid=12243");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/bahamas/;jsess", method.getResponseHeader("Location").getValue());
    }

    /**
     * note, had trouble keeping true utf (multi byte) chars as cvs buggers them up!
     */
    public void testTestUtf() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/utf/" + URLEncoder.encode("Fêtel'haïvolapük", "UTF8") + "/");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(getBaseUrl() + "/utf-redir/" + URLEncoder.encode("Fêtel'haïvolapük", "UTF8") + "/", method.getResponseHeader("Location").getValue());
    }

    public void testSimpleRun() throws ServletException, IOException {
        GetMethod method = new GetMethod(getBaseUrl() + "/run/test/test1");
        client.executeMethod(method);
        assertEquals("this is " + TestRunObj.class.getName(), method.getResponseBodyAsString());
    }

}
