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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.FilterConfig;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;


/**
 * Defines a the config that will be passed to the run object on startup.
 *
 * @author Paul Tuckey
 * @version $Revision: 26 $ $Date: 2006-09-05 22:21:17 +1200 (Tue, 05 Sep 2006) $
 */
public class RunConfig implements ServletConfig, FilterConfig {

    private ServletContext servletContext;
    private Hashtable<String, String> initParameters;

    public RunConfig(ServletContext servletContext, Map<String, String> initParameters) {
        this.servletContext = servletContext;
        this.initParameters = new Hashtable<>(initParameters);
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
        return initParameters.get(s);
    }

    public Enumeration getInitParameterNames() {
        return initParameters.keys();
    }
}
