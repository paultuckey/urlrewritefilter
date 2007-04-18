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
package org.tuckey.web.filters.urlrewrite;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


public class UrlRewriteWrappedRequest extends HttpServletRequestWrapper {
    private HashMap parameters = new HashMap();
    private Set replacedParams = new HashSet();

    public UrlRewriteWrappedRequest(HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
    }

    public void setParameter(String name, String value) {
        parameters.put(name, value);
        replacedParams.add(name);
    }

    public String getParameter(String name) {
        if ( ! replacedParams.contains(name)) {
            return super.getParameter(name);
        }
        return (String) parameters.get(name);
    }
}
