package org.tuckey.web.filters.urlrewrite.extend;

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.Run;
import org.json.JSONString;
import org.json.JSONObject;
import org.json.JSONException;

import javax.servlet.ServletException;
import java.io.StringWriter;
import java.io.IOException;
import java.util.Date;


public class RewriteMatchJsonTest extends TestCase {

    public void testJson() throws ServletException, IOException {
        RewriteMatchJson rmj = new RewriteMatchJson("hello");

        StringWriter sw = new StringWriter();
        rmj.writeJsonObject("hello", sw);
        assertEquals("{\"result\":\"hello\",\"id\":0}", sw.toString());

        StringWriter sw2 = new StringWriter();
        rmj.writeJsonObject(new Long(99), sw2);
        assertEquals("{\"result\":99,\"id\":0}", sw2.toString());

    }

    public void testJson3() throws ServletException, IOException {
        RewriteMatchJson rmj = new RewriteMatchJson("hello");
        StringWriter sw3 = new StringWriter();
        rmj.writeJsonObject(new Run(), sw3);
        assertEquals("{\"result\":{\"newEachTime\":false," +
                "\"javaClass\":\"org.tuckey.web.filters.urlrewrite.Run\"," +
                "\"handler\":\"standard\",\"methodSignature\":\"run\"," +
                "\"methodStr\":\"run\",\"valid\":false,\"error\":null," +
                "\"id\":0,\"filter\":false,\"initialised\":false," +
                "\"classStr\":null,\"runClassInstance\":null," +
                "\"displayName\":\"Run 0\"},\"id\":0}", sw3.toString());
    }


    class JSONStringSample implements JSONString {
        public String toJSONString() {
            JSONObject jo = new JSONObject();
            try {
                jo.put("stringSample", "hello");
                jo.put("longSample", new Long(0));
                jo.put("dateSample", new Date(1234567890).getTime());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jo.toString();
        }
    }

    public void testJson4() throws ServletException, IOException {
        RewriteMatchJson rmj = new RewriteMatchJson("hello");
        StringWriter sw3 = new StringWriter();
        rmj.writeJsonObject(new JSONStringSample(), sw3);
        assertEquals("{\"result\":{{\"dateSample\":1234567890,\"longSample\":0," +
                "\"stringSample\":\"hello\"}},\"id\":0}", sw3.toString());
    }

    public void testJsonError() throws ServletException, IOException {
        RewriteMatchJson rmj = new RewriteMatchJson("hello");
        StringWriter sw3 = new StringWriter();
        rmj.writeJsonObject(new NullPointerException(), sw3);
        assertTrue("not right: " + sw3.toString(), sw3.toString().startsWith("{\"error\":{\"code\":490," +
                "\"trace\":\"java.lang.NullPointerException\\r\\n\\t" +
                "at org.tuckey.web.filters.urlrewrite.extend.RewriteMatchJsonTest"));
    }

}
