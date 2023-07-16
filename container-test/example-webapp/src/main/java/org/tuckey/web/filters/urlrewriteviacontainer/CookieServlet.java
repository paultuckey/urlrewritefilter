
package org.tuckey.web.filters.urlrewriteviacontainer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

public class CookieServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Writer writer = resp.getWriter()) {
            writer.write("Cookie-Servlet\n");
            for(Cookie cookie: req.getCookies()) {
                writer.write(cookie.getName());
                writer.write((": "));
                writer.write(cookie.getValue());
            }
        }
    }
}