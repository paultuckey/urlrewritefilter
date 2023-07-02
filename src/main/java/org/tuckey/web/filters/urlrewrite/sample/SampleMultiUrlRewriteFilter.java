/**
 * Copyright (c) 2005-2023, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * - Neither the name tuckey.org nor the names of its contributors
 * may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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
package org.tuckey.web.filters.urlrewrite.sample;

import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample of how you might load multiple configuration files. (NOT to be used verbatim!!)
 */
public class SampleMultiUrlRewriteFilter extends UrlRewriteFilter {

    private List urlrewriters = new ArrayList();
     
    public void loadUrlRewriter(FilterConfig filterConfig) throws ServletException {
        // add configurations
        try {
            Conf conf1 = new Conf(filterConfig.getServletContext(), new FileInputStream("someconf.xml"), "someconf.xml", "");
            urlrewriters.add(new UrlRewriter(conf1));

            Conf conf2 = new SampleConfExt();
            urlrewriters.add(new UrlRewriter(conf2));

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public UrlRewriter getUrlRewriter(ServletRequest request, ServletResponse response, FilterChain chain) {
        // do some logic to decide what urlrewriter to use (possibly do a reload check on the conf file)
        return (UrlRewriter) urlrewriters.get(0);
    }

    public void destroyUrlRewriter() {
        for (int i = 0; i < urlrewriters.size(); i++) {
            UrlRewriter urlRewriter = (UrlRewriter) urlrewriters.get(i);
            urlRewriter.destroy();
        }
    }

}
