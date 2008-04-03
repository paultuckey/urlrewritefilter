package org.tuckey.web.filters.urlrewrite.json;

import junit.framework.TestCase;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.testhelper.MockResponse;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import java.io.IOException;

/**
 * todo: future: json work on hold for now
 */
public class JSONProcessorTest extends TestCase {


    public void testSimple() throws IOException {
        Log.setLevel("SYSOUT:DEBUG");
        JSONProcessor processor = new JSONProcessor();
        processor.setBase("org.tuckey.web.filters");
        MockRequest request = new MockRequest("urlrewrite/annotation/SampleMatchAction");
        MockResponse response = new MockResponse();
        request.setBody("{\n" +
                "    \"version\" : \"1.1\",\n" +
                "    \"method\"  : \"rpcGetClient2\",\n" +
                "    \"params\"  : { \"a\" : 12, \"b\" : 34, \"c\" : 56 }\n" +
                "}");
        processor.service(request, response);
        assertEquals("{\"result\":{\"javaClass\":\"org.tuckey.web.filters.urlrewrite.annotation.SampleMatchAction$ClientBean\"},\"version\":\"1.1\"}", response.getOutputStreamAsString());
    }


    public void testEcho() throws IOException {
        Log.setLevel("SYSOUT:DEBUG");
        JSONProcessor processor = new JSONProcessor();
        processor.setBase("org.tuckey.web.filters");
        MockRequest request = new MockRequest("urlrewrite/annotation/SampleMatchAction");
        MockResponse response = new MockResponse();
        request.setBody("{\n" +
                "    \"version\" : \"1.1\",\n" +
                "    \"method\"  : \"echo\",\n" +
                "    \"params\":[[1, 2, 3]]\n" +
                "}");
        processor.service(request, response);
        assertEquals("{\"result\":[\"1\",\"2\",\"3\"],\"id\":5,\"version\":\"1.1\"}", response.getOutputStreamAsString());
    }


}
