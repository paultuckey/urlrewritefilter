
package org.tuckey.web.filters.urlrewriteviacontainer;

import java.io.IOException;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SettestServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Writer writer = resp.getWriter();
        try {
            writer.write(String.format(
                "testsession: %s, param.settest1: %s, method: %s",
                req.getSession().getAttribute("testsession"),
                req.getParameter("settest1"),
                req.getMethod()
            ));
        } finally {
            writer.close();
        }
    }
}
