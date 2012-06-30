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
package org.tuckey.web.filters.urlrewrite.json;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonRewriteMatch extends RewriteMatch {

    private Object returned;
    private Throwable throwable;

    public JsonRewriteMatch(Object returned) {
        this.returned = returned;
    }

    public JsonRewriteMatch(Throwable throwable) {
        this.throwable = throwable;
    }

    public boolean execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.addHeader("Content-Type", "application/json");
        String jsonString = toJSONString(returned, throwable);
        response.setContentLength(jsonString.length());
        response.getOutputStream().write(jsonString.getBytes());
        return true; // return true, we handled the result
    }

    public String toJSONString(Object resultantObject, Throwable resultantThrowable) {
        JsonWriter writer = new JsonWriter();
        JsonRpcBean bean = new JsonRpcBean();
        bean.setResult(resultantObject);
        if (resultantThrowable != null) {
            JsonRpcErrorBean error = new JsonRpcErrorBean();
            error.setName(resultantThrowable.getClass().getName());
            error.setMessage(resultantThrowable.getMessage());
            error.setError(resultantThrowable.toString());
            bean.setError(error);
        }
        return writer.write(bean);
    }


}


class JsonRpcErrorBean {

    private String name;
    private String message;
    private Object error;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }
}

class JsonRpcBean {

    private Object result;
    private JsonRpcErrorBean error;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public JsonRpcErrorBean getError() {
        return error;
    }

    public void setError(JsonRpcErrorBean error) {
        this.error = error;
    }

}
