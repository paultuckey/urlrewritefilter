package org.tuckey.web.filters.urlrewrite.container;

import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;


public class ContainerTestBase extends TestCase {

    private String baseUrl = "http://127.0.0.1:8080";
    protected HttpClient client = new HttpClient();

    protected void setUp() throws Exception {
        String systemPropBaseUrl = System.getProperty("test.base.url");
        if (systemPropBaseUrl != null) {
            baseUrl = systemPropBaseUrl;
        }
    }

    protected String getBaseUrl() {
        return baseUrl;
    }
}
