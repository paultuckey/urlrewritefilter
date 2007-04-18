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

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Servlet for use if you cannot load filters in your environment for some strange reason.  This will
 * work very differently from the filter (obviously), but it can be used for "run" items where there
 * is no "to".
 *
 * This servlet just loads an instance of the filter and calls it as if it were the container.
 */
public class UrlRewriteServlet extends HttpServlet {

    private UrlRewriteFilter urlRewriteFilter = new UrlRewriteFilter();

    public void init(ServletConfig servletConfig) throws ServletException {

        urlRewriteFilter.init(new ConfigWrapper(servletConfig));
    }

    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!urlRewriteFilter.isLoaded()) {
            throw new UnavailableException("not initialised");

        } else {
            FilterChainWrapper filterChainWrapper = new FilterChainWrapper();
            urlRewriteFilter.doFilter(request, response, filterChainWrapper);

        }
    }

    public void destroy() {
        urlRewriteFilter.destroy();
    }


    /**
     * Simple empty wrapper on top of FilterChain.
     */
    class FilterChainWrapper implements FilterChain {

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
                throws IOException, ServletException {
            // do nothing!
        }
    }

    /**
     * Simple wrapper for filter config to make it useful when we have a ServletConfig.
     */
    class ConfigWrapper implements FilterConfig {

        private ServletConfig servletConfig;

        public ConfigWrapper(ServletConfig servletConfig) {
            this.servletConfig = servletConfig;
        }

        public String getFilterName() {
            return this.servletConfig.getServletName();
        }

        public ServletContext getServletContext() {
            return servletConfig.getServletContext();
        }

        public String getInitParameter(String string) {
            return servletConfig.getInitParameter(string);
        }

        public Enumeration getInitParameterNames() {
            return servletConfig.getInitParameterNames();
        }
    }
}
