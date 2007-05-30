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
package org.tuckey.web.testhelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class MockResponse implements HttpServletResponse {

    private Hashtable responseHeaders = new Hashtable();
    private int status = 200;
    private String redirectedUrl;
    private List cookies = new ArrayList();
    private Locale locale;
    MockSerlvetOutputStream mockSerlvetOutputStream = new MockSerlvetOutputStream();
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public boolean containsHeader(String s) {
        return false;
    }

    public String encodeURL(String s) {
        if (s == null) return null;
        if (s.indexOf("http:") == 0 ) return s;
        if (s.indexOf("?") != -1) {
            return s.substring(0, s.indexOf("?")) + ";mockencoded=test" + s.substring(s.indexOf("?"), s.length());
        } else {
            return s.concat(";mockencoded=test");
        }
    }

    public String encodeRedirectURL(String s) {
        return encodeURL(s);
    }

    /**
     * @deprecated
     */
    public String encodeUrl(String s) {
        return encodeURL(s);
    }

    /**
     * @deprecated
     */
    public String encodeRedirectUrl(String s) {
        return encodeURL(s);
    }

    public void sendError(int i, String s) throws IOException {

    }

    public void sendError(int i) throws IOException {

    }

    public void sendRedirect(String s) throws IOException {
        redirectedUrl = s;
    }

    public void setDateHeader(String s, long l) {
        responseHeaders.put(s, l + "");
    }

    public void addDateHeader(String s, long l) {
        responseHeaders.put(s, l + "");
    }

    public void setHeader(String s, String s1) {
        responseHeaders.put(s, s1);
    }

    public void addHeader(String s, String s1) {
        responseHeaders.put(s, s1);
    }

    public void setIntHeader(String s, int i) {
        responseHeaders.put(s, i + "");
    }

    public void addIntHeader(String s, int i) {
        responseHeaders.put(s, i + "");
    }

    public void setStatus(int i) {
        status = i;
    }

    /**
     * @deprecated
     */
    public void setStatus(int i, String s) {

    }

    public String getCharacterEncoding() {
        return null;
    }

    public String getContentType() {
        return null;
    }

    public ServletOutputStream getOutputStream() throws IOException {
        return mockSerlvetOutputStream;
    }

    public String getOutputStreamAsString() {
        return mockSerlvetOutputStream.getAsString();
    }

    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    public String getWriterAsString() {
        writer.flush();
        return stringWriter.toString();
    }

    public void setCharacterEncoding(String s) {

    }

    public void setContentLength(int i) {

    }

    public void setContentType(String s) {

    }

    public void setBufferSize(int i) {

    }

    public int getBufferSize() {
        return 0;
    }

    public void flushBuffer() throws IOException {

    }

    public void resetBuffer() {

    }

    public boolean isCommitted() {
        return false;
    }

    public void reset() {

    }

    public void setLocale(Locale l) {
        locale = l;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getHeader(String s) {
        return (String) responseHeaders.get(s);
    }

    public int getStatus() {
        return status;
    }

    public String getRedirectedUrl() {
        return redirectedUrl;
    }

    public List getCookies() {
        return cookies;
    }
}

class MockSerlvetOutputStream extends ServletOutputStream {

    ByteArrayOutputStream baos;

    public MockSerlvetOutputStream() {
        this.baos = new ByteArrayOutputStream();
    }

    public void write(int b) throws IOException {
        baos.write(b);
    }

    public String getAsString() {
        return new String(baos.toByteArray());
    }
}