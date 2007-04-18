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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.FilterConfig;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 * Defines a the config that will be passed to the run object on startup.
 *
 * @author Paul Tuckey
 * @version $Revision: 26 $ $Date: 2006-09-05 22:21:17 +1200 (Tue, 05 Sep 2006) $
 */
public class RunConfig implements ServletConfig, FilterConfig {

    private ServletContext servletContext;
    private Hashtable initParameters;

    public RunConfig(ServletContext servletContext, Hashtable initParameters) {
        this.servletContext = servletContext;
        this.initParameters = initParameters;
    }

    public String getServletName() {
        return null;
    }

    public String getFilterName() {
        return null;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public String getInitParameter(String s) {
        return (String) initParameters.get(s);
    }

    public Enumeration getInitParameterNames() {
        return initParameters.keys();
    }
}
