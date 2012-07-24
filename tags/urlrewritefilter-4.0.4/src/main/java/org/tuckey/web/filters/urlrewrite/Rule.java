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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.lang.reflect.InvocationTargetException;


public interface Rule {

    /**
     * Will run the rule against the uri and perform action required will return false is not matched
     * otherwise true.
     *
     * @param url
     * @param hsRequest
     * @return String of the rewritten url or the same as the url passed in if no match was made
     */
    public RewrittenUrl matches(final String url, final HttpServletRequest hsRequest,
                                final HttpServletResponse hsResponse, final RuleChain chain)
            throws IOException, ServletException, InvocationTargetException;

    public RewrittenUrl matches(final String url, final HttpServletRequest hsRequest,
                                final HttpServletResponse hsResponse)
            throws IOException, ServletException, InvocationTargetException;

    /**
     * Will initialise the rule.
     *
     * @return true on success
     */
    public boolean initialise(ServletContext context);

    public void destroy();

    public String getName();

    public String getDisplayName();

    public boolean isLast();

    public void setId(int i);

    public int getId();

    public boolean isValid();

    public boolean isFilter();

    /**
     * List of strings for all errors.
     */
    public List getErrors();

}
