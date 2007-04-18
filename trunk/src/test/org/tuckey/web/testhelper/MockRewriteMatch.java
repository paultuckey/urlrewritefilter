package org.tuckey.web.testhelper;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class MockRewriteMatch extends RewriteMatch {

    private static long calledTime = 0;

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            //
        }
        calledTime = System.currentTimeMillis();
        return true;
    }

    public static long getCalledTime() {
        return calledTime;
    }
}
