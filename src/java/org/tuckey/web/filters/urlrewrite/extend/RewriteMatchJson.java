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
package org.tuckey.web.filters.urlrewrite.extend;

import com.metaparadigm.jsonrpc.JSONRPCResult;
import com.metaparadigm.jsonrpc.JSONSerializer;
import com.metaparadigm.jsonrpc.MarshallException;
import com.metaparadigm.jsonrpc.SerializerState;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;


/**
 * todo: document
 */
public class RewriteMatchJson extends RewriteMatch {

    private static Log log = Log.getLog(RewriteMatchJson.class);

    Object returnedObject;

    public RewriteMatchJson(Object returnedObject) {
        this.returnedObject = returnedObject;
    }

    public boolean execute(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object result = executeAction(request, response);
        if (result == null) return false;
        Writer writer = response.getWriter();
        writeJsonObject(result, writer);
        return true;
    }

    protected void writeJsonObject(Object result, Writer writer) throws ServletException, IOException {
        if (writer == null) throw new ServletException("writer is null");

        Long requestId = new Long(0);
        JSONRPCResult jsonResult;
        JSONSerializer serializer = new JSONSerializer();
        try {
            try {
                serializer.registerDefaultSerializers();
            } catch (Exception e) {
                log.error(e, e);
            }
            serializer.setMarshallClassHints(true);
            serializer.setMarshallNullAttributes(true);

            if (result != null && (result instanceof Throwable)) {
                if (result instanceof InvocationTargetException) {
                    result = ((InvocationTargetException) result).getTargetException();
                }
                jsonResult = new JSONRPCResult(JSONRPCResult.CODE_REMOTE_EXCEPTION, requestId, result);

            } else {
                SerializerState state = new SerializerState();
                Object marshaledObject = serializer.marshall(state, result);
                jsonResult = new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId, marshaledObject);
            }

        } catch (MarshallException e) {
            jsonResult = new JSONRPCResult(JSONRPCResult.CODE_ERR_MARSHALL, requestId, e.getMessage());
        }
        writer.write(jsonResult.toString());
    }

    public Object executeAction(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        return returnedObject;
    }


}
