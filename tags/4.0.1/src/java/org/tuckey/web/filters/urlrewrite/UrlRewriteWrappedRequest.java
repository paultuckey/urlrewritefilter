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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles wrapping the request if necessary.
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class UrlRewriteWrappedRequest extends HttpServletRequestWrapper {

    HashMap overridenParameters;
    String overridenMethod;

    public UrlRewriteWrappedRequest(HttpServletRequest httpServletRequest) {
        super(httpServletRequest);
    }

    public UrlRewriteWrappedRequest(HttpServletRequest httpServletRequest,
                                    HashMap overridenParameters, String overridenMethod) {
        super(httpServletRequest);
        this.overridenParameters = overridenParameters;
        this.overridenMethod = overridenMethod;
    }

    public Enumeration getParameterNames() {
        if (overridenParameters != null) {
            List keys = Collections.list(super.getParameterNames());
            keys.addAll(overridenParameters.keySet());
            return Collections.enumeration(keys);
        }
        return super.getParameterNames();
    }

    public Map getParameterMap() {
        if (overridenParameters != null) {
            Map superMap = super.getParameterMap();
            //superMap is an unmodifiable map, hence creating a new one.
            Map overriddenMap = new HashMap(superMap.size() + overridenParameters.size());
            overriddenMap.putAll(superMap);
            overriddenMap.putAll(overridenParameters);
            return overriddenMap;
        }
        return super.getParameterMap();
    }

    public String[] getParameterValues(String s) {
        if (overridenParameters != null && overridenParameters.containsKey(s)) {
            return (String[]) overridenParameters.get(s);
        }
        return super.getParameterValues(s);
    }

    public String getParameter(String s) {
        if (overridenParameters != null && overridenParameters.containsKey(s)) {
            String[] values = (String[]) overridenParameters.get(s);
            if (values == null || values.length == 0) {
                return null;
            } else {
                return values[0];
            }
        }
        return super.getParameter(s);
    }

    public String getMethod() {
        if (overridenMethod != null) return overridenMethod;
        return super.getMethod();
    }
}
