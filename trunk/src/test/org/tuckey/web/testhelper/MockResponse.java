/**
 * Copyright (c) 2005, Paul Tuckey
 * All rights reserved.
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package org.tuckey.web.testhelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
        return null;
    }

    public PrintWriter getWriter() throws IOException {
        return null;
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
