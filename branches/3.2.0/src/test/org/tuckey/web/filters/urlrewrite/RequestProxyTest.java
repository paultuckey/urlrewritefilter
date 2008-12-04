package org.tuckey.web.filters.urlrewrite;

import junit.framework.TestCase;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Paul Tuckey
 * @version $Revision: 40 $ $Date: 2006-10-27 15:12:37 +1300 (Fri, 27 Oct 2006) $
 */
public class RequestProxyTest extends TestCase {


    public void testUseProxyServer() throws IOException, ServletException {
        assertEquals(3128, RequestProxy.getUseProxyServer("myproxyserver:3128").getPort());
        assertEquals(80, RequestProxy.getUseProxyServer("myproxyserver:A3128").getPort());
        assertEquals("myproxyserver", RequestProxy.getUseProxyServer("myproxyserver:A3128").getHostName());
    }


}
