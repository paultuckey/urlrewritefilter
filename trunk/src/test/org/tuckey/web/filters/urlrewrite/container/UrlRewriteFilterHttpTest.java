package org.tuckey.web.filters.urlrewrite.container;

import junit.framework.TestCase;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.tuckey.web.filters.urlrewrite.TestRunObj;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import org.xml.sax.SAXException;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;


public class UrlRewriteFilterHttpTest extends TestCase {

    private String CONTENT = "<p>some content</p>";

    private String baseUrl = "http://127.0.0.1:8080/webapp";
    HttpClient client = new HttpClient();

    protected void setUp() throws Exception {
        String systemPropBaseUrl = System.getProperty("test.base.url");
        if (systemPropBaseUrl != null) {
            baseUrl = systemPropBaseUrl;
        }
    }

    public void testProduct() throws IOException, SAXException, InterruptedException {
        GetMethod method = new GetMethod(baseUrl + "/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }


    public void testSimpleDistEx() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(baseUrl + "/test/status/");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(baseUrl + "/rewrite-status", method.getResponseHeader("Location").getValue());
    }

    public void testBasicSets() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(baseUrl + "/settest/674");
        client.executeMethod(method);
        assertNotNull(method.getResponseHeader("cache-control"));
        //todo: assertEquals("hello!", request.getSession().getAttribute("testsession"));
    }

    public void testMultipleProduct() throws ServletException, IOException, SAXException {
        GetMethod method = new GetMethod(baseUrl + "/multiple/products/987");
        client.executeMethod(method);
        assertEquals("product 987", method.getResponseBodyAsString());
    }

    public void testNullTo() throws ServletException, IOException {
        GetMethod method = new GetMethod(baseUrl + "/hideme/anb.jsp;dsaddd?asdasds#sdsfd");
        client.executeMethod(method);
        assertEquals("should have status set", 403, method.getStatusCode());
        assertFalse("should not output above content", CONTENT.equals(StringUtils.trim(method.getResponseBodyAsString())));
    }

    public void testYear() throws ServletException, IOException {
        GetMethod method = new GetMethod(baseUrl + "/time/year/current");
        client.executeMethod(method);
        assertEquals("echo yearisbetween1970and3000", method.getResponseBodyAsString());
    }

    public void testTestAxis() throws ServletException, IOException {
        GetMethod method = new GetMethod(baseUrl + "/services/blah?qwerty");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(baseUrl + "/axis/services/blah", method.getResponseHeader("Location").getValue());
    }

    public void testTestErik() throws ServletException, IOException {
        GetMethod method = new GetMethod(baseUrl + "/eriktest/hi.ho");
        method.setFollowRedirects(false);
        method.addRequestHeader(new Header("host", "blah.com"));
        client.executeMethod(method);
        assertEquals("http://www.example.com/context/hi.ho", method.getResponseHeader("Location").getValue());
    }

    /**
     * todo: fix!!!
     *
     * @throws ServletException
     * @throws IOException
     */
    public void testTestEncode() throws ServletException, IOException {
        GetMethod method = new GetMethod(baseUrl + "/went%20to%20bahamas/;jsessionid=12243");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(baseUrl + "/bahamas/;jsess", method.getResponseHeader("Location").getValue());
    }

    /**
     * note, had trouble keeping true utf (multi byte) chars as cvs buggers them up!
     */
    public void testTestUtf() throws ServletException, IOException {
        GetMethod method = new GetMethod(baseUrl + "/utf/" + URLEncoder.encode("Fêtel'haïvolapük", "UTF8") + "/");
        method.setFollowRedirects(false);
        client.executeMethod(method);
        assertEquals(baseUrl + "/utf-redir/" + URLEncoder.encode("Fêtel'haïvolapük", "UTF8") + "/", method.getResponseHeader("Location").getValue());
    }

    public void testSimpleRun() throws ServletException, IOException {
        GetMethod method = new GetMethod(baseUrl + "/run/test/test1");
        client.executeMethod(method);
        //todo: assertTrue("should be inited", TestRunObj.isInitCalled());
        assertEquals("this is " + TestRunObj.class.getName(), method.getResponseBodyAsString());
    }

}
