package org.tuckey.web.filters.urlrewrite.sample;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * A sample of how you might write a custom match.
 */
class SampleRewriteMatch extends RewriteMatch {
    private int id;

    SampleRewriteMatch(int i) {
        id = i;
    }

    int getId() {
        return id;
    }

    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // lookup things in the db based on id

        // do something like forward to a jsp
        request.setAttribute("sampleRewriteMatch", this);
        RequestDispatcher rd = request.getRequestDispatcher("/WEB-INF/jsp/some-view.jsp");
        rd.forward(request, response);
        // in the jsp you can use request.getAttribute("sampleRewriteMatch") to fetch this object

        return true;
    }

}