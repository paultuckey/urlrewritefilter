/**
 * Copyright (c) 2005-2007, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name tuckey.org nor the names of its contributors
 *     may be used to endorse or promote products derived from this
 *     software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.tuckey.web.filters.urlrewrite.annotation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @HttpUrl("^/search/(clients|staff)/$")
    public void addClientFilterSecond(String searchType, @HttpParam String firstName,
                                      @HttpParam("lName")String lastName)
            throws SQLException {

    }

    /**
     * In the file 3rd. Should be 1st.
     * <p/>
     * Multiline doc.
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

    /**
     * In file 5th. Should be 2nd.
     */
    @HttpUrl(value = "^/clientinfo/(*)/$", weight = 1)
    public void addClientFilterSecond(int clientId, FilterChain chain)
            throws SQLException {

    }

    class ClientBean {
        private String name = "Bob";
    }

    @HttpExceptionHandler("java.lang.Exception")
    public void addExceptionHandler(Exception e)
            throws SQLException {

    }

    @HttpExceptionHandler
    public void addExceptionHandlerAuto(Exception e)
            throws SQLException {

    }

    @HttpJson
    public String clientDetailsRpc(@HttpParam int clientId)
            throws SQLException {
        return "Hello World";
    }

}
