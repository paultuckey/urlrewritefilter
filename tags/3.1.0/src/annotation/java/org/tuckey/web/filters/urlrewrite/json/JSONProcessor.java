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

import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Processor for RPC requests.
 *
 * todo: future: json work on hold for now
 * 
 */
public class JSONProcessor {

    private static final Log log = Log.getLog(JSONProcessor.class);

    String base = "/rpc/";

    public void setBase(String base) {
        this.base = base;
    }

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        log.info("servicing " + uri);
        Object returned = null;
        Throwable throwable = null;
        if ( uri.contains("/")) uri = uri.replace('/','.');
        if ( uri.startsWith(base)) uri = uri.substring(base.length());
        else uri = base + '.' + uri;
        String fullyQualifiedClassName = uri;
        if ( uri.contains(":") ) fullyQualifiedClassName = uri.substring(0, uri.indexOf(':'));
        log.info("class " + fullyQualifiedClassName);
        Class classToAccess = null;
        try {
            classToAccess = Class.forName(fullyQualifiedClassName);
        } catch (ClassNotFoundException e) {
            throwable = e;
        }
        if (classToAccess != null) {
            BufferedReader br = request.getReader();
            StringBuffer sb = new StringBuffer();
            while (br.ready()) {
                sb.append((char) br.read());
            }

            JSONReader reader = new JSONReader();
            Object obj = reader.read(sb.toString());
            log.debug(sb);
            if (obj instanceof HashMap) {
                log.debug("got hashmap ok");
                HashMap requestRpcObj = (HashMap) obj;
                String methodToAccess = (String) requestRpcObj.get("method");
                HashMap params = (HashMap) requestRpcObj.get("params");
                Method methodFound = null;
                Method[] methods = classToAccess.getMethods();
                for (Method method : methods) {
                    if (method.getName().equalsIgnoreCase(methodToAccess)) {
                        if (methodFound != null)
                            log.error("multiple methods found with same name (" + methodToAccess + "), this is not supported");
                        else methodFound = method;
                    }
                }
                if (methodFound != null) {
                    log.info("found method " + methodFound);
                    Object[] paramsToCallWith = new Object[methodFound.getParameterTypes().length];
                    Object[] paramValues = params.values().toArray();
                    for (int i = 0; i < methodFound.getParameterTypes().length; i++) {
                        Class methodParam = methodFound.getParameterTypes()[i];
                        Object paramValue = paramValues.length >= i ? paramValues[i] : null;
                        if ( String.class.equals(methodParam) && paramValue != null ) paramValue = paramValue.toString();
                        paramsToCallWith[i] = getConvertedParam(methodParam, paramValue);
                    }
                    try {
                        Object instance = classToAccess.getConstructor().newInstance((Object[]) null);
                        returned = methodFound.invoke(instance, paramsToCallWith);
                    } catch (Throwable e) {
                        throwable = e;
                    }
                } else {
                    log.error("method not found");
                }
            }
        }
        response.addHeader("Content-Type", "application/json");
        String jsonString = toJSONString(returned, throwable);
        response.setContentLength(jsonString.length());
        response.getOutputStream().write(jsonString.getBytes());
    }

    public String toJSONString(Object resultantObject, Throwable resultantThrowable) {
        JSONWriter writer = new JSONWriter();
        JSONRPCBean bean = new JSONRPCBean();
        bean.setResult(resultantObject);
        if (resultantThrowable != null) {
            JSONRPCErrorBean error = new JSONRPCErrorBean();
            error.setMessage(resultantThrowable.toString());
            error.setCode(500);
            error.setError(resultantThrowable);
            bean.setError(error);
        }
        return writer.write(bean);
    }

    public static Object getConvertedParam(Class runMethodParam, Object matchObj) {
        // see http://jakarta.apache.org/commons/beanutils/api/org/apache/commons/beanutils/MethodUtils.html
        // for how to handle methods better
        Object param = null;
        if (matchObj == null) {
            if (runMethodParam.isPrimitive()) {
                if (runMethodParam.equals(boolean.class)) param = Boolean.FALSE;
                else if (runMethodParam.equals(char.class)) param = '\u0000';
                else if (runMethodParam.equals(byte.class)) param = (byte) 0;
                else if (runMethodParam.equals(short.class)) param = (short) 0;
                else if (runMethodParam.equals(int.class)) param = 0;
                else if (runMethodParam.equals(long.class)) param = 0L;
                else if (runMethodParam.equals(float.class)) param = 0f;
                else if (runMethodParam.equals(double.class)) param = 0d;
            }
        } else {
            if (runMethodParam.equals(Boolean.class) || runMethodParam.equals(boolean.class))
                param = Boolean.valueOf((String) matchObj);
            else if (runMethodParam.equals(Character.class) || runMethodParam.equals(char.class))
                param = ((String) matchObj).charAt(0);
            else if (runMethodParam.equals(Byte.class) || runMethodParam.equals(byte.class))
                param = Byte.valueOf((String) matchObj);
            else if (runMethodParam.equals(Short.class) || runMethodParam.equals(short.class))
                param = Short.valueOf((String) matchObj);
            else if (runMethodParam.equals(Integer.class) || runMethodParam.equals(int.class))
                param = Integer.valueOf((String) matchObj);
            else if (runMethodParam.equals(Long.class) || runMethodParam.equals(long.class))
                param = Long.valueOf((String) matchObj);
            else if (runMethodParam.equals(Float.class) || runMethodParam.equals(float.class))
                param = Float.valueOf((String) matchObj);
            else if (runMethodParam.equals(Double.class) || runMethodParam.equals(double.class))
                param = Double.valueOf((String) matchObj);
            else if (matchObj != null &&
                    matchObj instanceof Throwable &&
                    runMethodParam.isAssignableFrom(matchObj.getClass()))
                param = matchObj;
            else {
                try {
                    // last attempt
                    param = runMethodParam.cast(matchObj);
                } catch (ClassCastException e) {
                    // do nothing
                }
            }
        }
        return param;
    }

}
