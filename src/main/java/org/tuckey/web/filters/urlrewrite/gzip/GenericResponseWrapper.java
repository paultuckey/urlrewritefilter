package org.tuckey.web.filters.urlrewrite.gzip;

/**
 * Modified version of:
 * http://svn.terracotta.org/svn/ehcache/trunk/web/web/src/main/java/net/sf/ehcache/constructs/web/GenericResponseWrapper.java
 *
 *
 *  Copyright 2003-2009 Terracotta, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import javax.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.tuckey.web.filters.urlrewrite.utils.Log;


/**
 * Provides a wrapper for {@link javax.servlet.http.HttpServletResponseWrapper}.
 * <p/>
 * It is used to wrap the real Response so that we can modify it after
 * that the target of the request has delivered its response.
 * <p/>
 * It uses the Wrapper pattern.
 *
 * @author <a href="mailto:gluck@thoughtworks.com">Greg Luck</a>
 * @version $Id: GenericResponseWrapper.java 793 2008-10-07 07:28:03Z gregluck $
 */
public class GenericResponseWrapper extends HttpServletResponseWrapper implements Serializable {

    private static final Log LOG = Log.getLog(GenericResponseWrapper.class);
    private int statusCode = SC_OK;
    private String contentType;
    private final Map<String, List<Serializable>> headersMap = new TreeMap<String, List<Serializable>>(String.CASE_INSENSITIVE_ORDER);
    private final List cookies = new ArrayList();
    private final ServletOutputStream outstr;
    private PrintWriter writer;
    private boolean disableFlushBuffer = true;

    /**
     * Creates a GenericResponseWrapper
     */
    public GenericResponseWrapper(final HttpServletResponse response, final OutputStream outstr) {
        super(response);
        this.outstr = new FilterServletOutputStream(outstr);
    }

    /**
     * Gets the OutputStream.
     */
    public ServletOutputStream getOutputStream() {
        return outstr;
    }

    /**
     * Sets the status code for this response.
     */
    public void setStatus(final int code) {
        statusCode = code;
        super.setStatus(code);
    }

    /**
     * Send the error. If the response is not ok, most of the logic is bypassed and the error is sent raw
     * Also, the content is not cached.
     *
     * @param i      the status code
     * @param string the error message
     * @throws IOException
     */
    public void sendError(int i, String string) throws IOException {
        statusCode = i;
        super.sendError(i, string);
    }

    /**
     * Send the error. If the response is not ok, most of the logic is bypassed and the error is sent raw
     * Also, the content is not cached.
     *
     * @param i the status code
     * @throws IOException
     */
    public void sendError(int i) throws IOException {
        statusCode = i;
        super.sendError(i);
    }

    /**
     * Send the redirect. If the response is not ok, most of the logic is bypassed and the error is sent raw.
     * Also, the content is not cached.
     *
     * @param string the URL to redirect to
     * @throws IOException
     */
    public void sendRedirect(String string) throws IOException {
        statusCode = HttpServletResponse.SC_MOVED_TEMPORARILY;
        super.sendRedirect(string);
    }

    /**
     * Sets the status code for this response.
     */
    public void setStatus(final int code, final String msg) {
        statusCode = code;
        LOG.warn("Discarding message because this method is deprecated.");
        super.setStatus(code);
    }

    /**
     * Returns the status code for this response.
     */
    public int getStatus() {
        return statusCode;
    }

    /**
     * Sets the content type.
     */
    public void setContentType(final String type) {
        this.contentType = type;
        super.setContentType(type);
    }

    /**
     * Gets the content type.
     */
    public String getContentType() {
        return contentType;
    }


    /**
     * Gets the print writer.
     */
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(outstr, getCharacterEncoding()), true);
        }
        return writer;
    }



    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#addHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void addHeader(String name, String value) {
        List<Serializable> values = this.headersMap.get(name);
        if (values == null) {
            values = new LinkedList<Serializable>();
            this.headersMap.put(name, values);
        }
        values.add(value);

        super.addHeader(name, value);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#setHeader(java.lang.String, java.lang.String)
     */
    @Override
    public void setHeader(String name, String value) {
        final LinkedList<Serializable> values = new LinkedList<Serializable>();
        values.add(value);
        this.headersMap.put(name, values);

        super.setHeader(name, value);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#addDateHeader(java.lang.String, long)
     */
    @Override
    public void addDateHeader(String name, long date) {
        List<Serializable> values = this.headersMap.get(name);
        if (values == null) {
            values = new LinkedList<Serializable>();
            this.headersMap.put(name, values);
        }
        values.add(date);

        super.addDateHeader(name, date);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#setDateHeader(java.lang.String, long)
     */
    @Override
    public void setDateHeader(String name, long date) {
        final LinkedList<Serializable> values = new LinkedList<Serializable>();
        values.add(date);
        this.headersMap.put(name, values);

        super.setDateHeader(name, date);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#addIntHeader(java.lang.String, int)
     */
    @Override
    public void addIntHeader(String name, int value) {
        List<Serializable> values = this.headersMap.get(name);
        if (values == null) {
            values = new LinkedList<Serializable>();
            this.headersMap.put(name, values);
        }
        values.add(value);

        super.addIntHeader(name, value);
    }

    /**
     * @see javax.servlet.http.HttpServletResponseWrapper#setIntHeader(java.lang.String, int)
     */
    @Override
    public void setIntHeader(String name, int value) {
        final LinkedList<Serializable> values = new LinkedList<Serializable>();
        values.add(value);
        this.headersMap.put(name, values);

        super.setIntHeader(name, value);
    }

    /**
     * Adds a cookie.
     */
    public void addCookie(final Cookie cookie) {
        cookies.add(cookie);
        super.addCookie(cookie);
    }

    /**
     * Flushes buffer and commits response to client.
     */
    public void flushBuffer() throws IOException {
        flush();

        // doing this might leads to response already committed exception
        // when the PageInfo has not yet built but the buffer already flushed
        // Happens in Weblogic when a servlet forward to a JSP page and the forward
        // method trigger a flush before it forwarded to the JSP
        // disableFlushBuffer for that purpose is 'true' by default
        // EHC-447
        if (!disableFlushBuffer) {
            super.flushBuffer();
        }
    }

    /**
     * Resets the response.
     */
    public void reset() {
        super.reset();
        cookies.clear();
        headersMap.clear();
        statusCode = SC_OK;
        contentType = null;
    }

    /**
     * Flushes all the streams for this response.
     */
    public void flush() throws IOException {
        if (writer != null) {
            writer.flush();
        }
        outstr.flush();
    }

    /**
     * Set if the wrapped reponse's buffer flushing should be disabled.
     */
    public void setDisableFlushBuffer() {
        this.disableFlushBuffer = true;
    }
}

