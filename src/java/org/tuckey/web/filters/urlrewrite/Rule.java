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
