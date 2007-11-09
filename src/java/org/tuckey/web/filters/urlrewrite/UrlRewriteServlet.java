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
    private static final long serialVersionUID = 2186203405866227539L;

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
    static class FilterChainWrapper implements FilterChain {

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
                throws IOException, ServletException {
            // do nothing!
        }
    }

    /**
     * Simple wrapper for filter config to make it useful when we have a ServletConfig.
     */
    static class ConfigWrapper implements FilterConfig {

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
