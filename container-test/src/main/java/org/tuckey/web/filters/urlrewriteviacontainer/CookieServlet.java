
package org.tuckey.web.filters.urlrewriteviacontainer;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CookieServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Writer writer = resp.getWriter();
        try {
            for(Cookie cookie: req.getCookies()) {
                writer.write(cookie.getName());
                writer.write((": "));
                writer.write(cookie.getValue());
            }
        } finally {
            writer.close();
        }
    }
}
