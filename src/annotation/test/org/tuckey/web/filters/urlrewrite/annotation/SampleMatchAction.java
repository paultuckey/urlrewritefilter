package org.tuckey.web.filters.urlrewrite.annotation;

import javax.servlet.ServletException;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;


public class SampleMatchAction {

    @HttpUrl(value = "^/1clientinfo/$")
    public void listActive(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {

    }

    /**
     * In file 2nd.
     */
    @HttpUrl("^/clientinfo/(*)/$")
    public void clientDetails(int clientId)
            throws SQLException {

    }

    /**
     * In the file 3rd. Should be 1st.
     */
    @HttpUrl(value = "^/clientinfo/(*)/$", weight = 2)
    public void addClientFilterFirst(int clientId, FilterChain chain)
            throws SQLException {

    }

    /**
     * Should be the last item in the processed file.
     * In file 4th. Should be last.
     */
    @HttpUrl(value = "^/clientinfo/(*)/$", weight = -1)
    public void addClientFilterLast(int clientId, FilterChain chain)
            throws SQLException {

    }

    // todo: add getParameter and post-param to condition type


    /**
     * In file 5th. Should be 2nd.
     */
    @HttpUrl(value = "^/clientinfo/(*)/$", weight = 1)
    public void addClientFilterSecond(int clientId, FilterChain chain)
            throws SQLException {

    }

    @HttpUrl("^/search/(clients|staff)/$")
    public void addClientFilterSecond(String searchType, @HttpParam String firstName, @HttpParam("lName") String lastName)
            throws SQLException {

    }

    @HttpExceptionHandler("java.lang.Exception")
    public void addExceptionHandler(Exception e)
            throws SQLException {

    }

    @HttpExceptionHandler
    public void addExceptionHandlerAuto(Exception e)
            throws SQLException {

    }

/*
    @HttpRule ({
        @Condition(type= "header", name = "userAgent", value = "(.*)"), // condition element
        @Param("firstName"), // or same as next line
        @Condition(type= "parameter", name = "firstName", value = "(.*)"),
        @From()
    }) // weight
    or
    @HttpUrl("regex to match request url ie, from eleent")  // weight
*/

}
