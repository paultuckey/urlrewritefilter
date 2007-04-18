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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class MockSession implements HttpSession {

    Hashtable attrs = new Hashtable();
    private boolean sessionNew;

    public long getCreationTime() {
        return 0;
    }

    public String getId() {
        return null;
    }

    public long getLastAccessedTime() {
        return 0;
    }

    public ServletContext getServletContext() {
        return null;
    }

    public void setMaxInactiveInterval(int i) {

    }

    public int getMaxInactiveInterval() {
        return 0;
    }

    /**
     * @deprecated
     */
    public HttpSessionContext getSessionContext() {
        return null;
    }

    public Object getAttribute(String s) {
        return attrs.get(s);
    }

    /**
     * @deprecated
     */
    public Object getValue(String s) {
        return null;
    }

    public Enumeration getAttributeNames() {
        return null;
    }

    /**
     * @deprecated
     */
    public String[] getValueNames() {
        return new String[0];
    }

    public void setAttribute(String s, Object o) {
        attrs.put(s, o);
    }

    /**
     * @deprecated
     */
    public void putValue(String s, Object o) {

    }

    public void removeAttribute(String s) {
        attrs.remove(s);
    }

    /**
     * @deprecated
     */
    public void removeValue(String s) {

    }

    public void invalidate() {

    }

    public boolean isNew() {
        return sessionNew;
    }

    public void setNew(boolean sessionNew) {
        this.sessionNew = sessionNew;
    }
}
