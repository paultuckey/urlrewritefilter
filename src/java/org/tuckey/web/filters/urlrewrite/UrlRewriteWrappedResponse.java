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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Handles wrapping the response so we can encode the url's on the way "out" (ie, in JSP or servlet generation).
 *
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class UrlRewriteWrappedResponse extends HttpServletResponseWrapper {

    private UrlRewriter urlRerwiter;
    private HttpServletResponse httpServletResponse;
    private HttpServletRequest httpServletRequest;

    public UrlRewriteWrappedResponse(HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest,
                                     UrlRewriter urlRerwiter) {
        super(httpServletResponse);
        this.httpServletResponse = httpServletResponse;
        this.httpServletRequest = httpServletRequest;
        this.urlRerwiter = urlRerwiter;
    }

    public String encodeURL(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeURL(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    public String encodeRedirectURL(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeRedirectURL(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    public String encodeUrl(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeUrl(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    public String encodeRedirectUrl(String s) {
        RewrittenOutboundUrl rou = processPreEncodeURL(s);
        if (rou == null) {
            return super.encodeURL(s);
        }
        if (rou.isEncode()) {
            rou.setTarget(super.encodeRedirectUrl(rou.getTarget()));
        }
        return processPostEncodeURL(rou.getTarget()).getTarget();
    }

    /**
     * Handle rewriting.
     *
     * @param s
     */
    private RewrittenOutboundUrl processPreEncodeURL(String s) {
        if (urlRerwiter == null) {
            return null;
        }
        return urlRerwiter.processEncodeURL(httpServletResponse, httpServletRequest, false, s);
    }

    /**
     * Handle rewriting after the containers encodeUrl has been called.
     *
     * @param s
     */
    private RewrittenOutboundUrl processPostEncodeURL(String s) {
        if (urlRerwiter == null) {
            return null;
        }
        return urlRerwiter.processEncodeURL(httpServletResponse, httpServletRequest, true, s);
    }

}
