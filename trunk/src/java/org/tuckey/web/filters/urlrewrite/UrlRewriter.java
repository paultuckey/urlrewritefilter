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

import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.List;

/**
 * The main rewriter.
 *
 * @author Paul Tuckey
 * @version $Revision: 52 $ $Date: 2007-02-26 07:00:28 +1300 (Mon, 26 Feb 2007) $
 */
public class UrlRewriter {

    private static Log log = Log.getLog(UrlRewriter.class);

    /**
     * The conf for this filter.
     */
    private Conf conf;

    public UrlRewriter(Conf conf) {
        this.conf = conf;
    }

    /**
     * Helpful for testing but otherwise, don't use.
     */
    public RewrittenUrl processRequest(final HttpServletRequest hsRequest,
                                       final HttpServletResponse hsResponse)
            throws IOException, ServletException, InvocationTargetException {
        RuleChain chain = getNewChain(hsRequest, null);
        if (chain == null) return null;
        chain.process(hsRequest, hsResponse);
        return chain.getFinalRewrittenRequest();
    }

    /**
     * The main method called for each request that this filter is mapped for.
     *
     * @param hsRequest The request to process.
     * @return returns true when response has been handled by url rewriter false when it hasn't.
     */
    public boolean processRequest(final HttpServletRequest hsRequest, final HttpServletResponse hsResponse,
                                  FilterChain parentChain)
            throws IOException, ServletException {
        RuleChain chain = getNewChain(hsRequest, parentChain);
        if (chain == null) return false;
        chain.doRules(hsRequest, hsResponse);
        return chain.isResponseHandled();
    }


    private RuleChain getNewChain(final HttpServletRequest hsRequest, FilterChain parentChain) {

        //String originalUrl = StringUtils.trim(hsRequest.getRequestURI());

        // prepare base part of url
        String servletPath = hsRequest.getServletPath();   // /servlet/MyServlet

        if (servletPath == null) {
            // for some reason the engine is not giving us the url
            // this isn't good
            log.debug("unable to fetch request uri from request.  This shouldn't happen, it may indicate that " +
                    "the web application server has a bug or that the request was not pased correctly.");
            return null;
        }

        String originalUrl = servletPath;
        if (log.isDebugEnabled()) {
            log.debug("processing request for " + originalUrl);
        }

        String pathInfo = hsRequest.getPathInfo();         // /a/b;c=123
        if (pathInfo != null) originalUrl = originalUrl + pathInfo;

        // add context if required
        String contextPath = hsRequest.getContextPath();
        if (contextPath != null && conf.isUseContext()) {
            log.debug("context added");
            originalUrl = contextPath + originalUrl;
        }

        // add the query string on uri (note, some web app containers do this)
        if (originalUrl != null && originalUrl.indexOf("?") == -1 && conf.isUseQueryString()) {
            String query = hsRequest.getQueryString();
            if (query != null) {
                query = query.trim();
                if (query.length() > 0) {
                    originalUrl = originalUrl + "?" + query;
                    log.debug("query string added");
                }
            }
        }

        // decode the url as required
        if (conf.isDecodeRequired()) {
            try {
                originalUrl = URLDecoder.decode(originalUrl, conf.getDecodeUsing());
                if (log.isDebugEnabled()) {
                    log.debug("after " + conf.getDecodeUsing() + " decoding " + originalUrl);
                }
            } catch (java.io.UnsupportedEncodingException e) {
                log.warn("the jvm doesn't seem to support decoding " + conf.getDecodeUsing() + ", matches may not occur correctly.");
                return null;
            }
        }

        if (!conf.isOk()) {
            // when conf cannot be loaded for some sort of error
            // continue as normal without looking at the non-existent rules
            log.debug("configuration is not ok.  not rewriting request.");
            return null;
        }

        final List rules = conf.getRules();
        if (rules.size() == 0) {
            // no rules defined
            log.debug("there are no rules setup.  not rewriting request.");
            return null;
        }

        return new RuleChain(this, originalUrl, parentChain);
    }


    /**
     * Handle an exception thrown by a Run element.
     */
    public RewrittenUrl handleInvocationTargetException(final HttpServletRequest hsRequest,
                                                        final HttpServletResponse hsResponse, InvocationTargetException e)
            throws ServletException, IOException {

        Throwable originalThrowable = getOriginalException(e);

        if (log.isDebugEnabled()) {
            log.debug("attampting to find catch for exception " + originalThrowable.getClass().getName());
        }

        List catchElems = conf.getCatchElems();
        for (int i = 0; i < catchElems.size(); i++) {
            CatchElem catchElem = (CatchElem) catchElems.get(i);
            if (!catchElem.matches(originalThrowable)) continue;
            try {
                return catchElem.execute(hsRequest, hsResponse, originalThrowable);

            } catch (InvocationTargetException invocationExceptionInner) {
                originalThrowable = getOriginalException(invocationExceptionInner);
                log.warn("had exception processing catch, trying the rest of the catches with " +
                        originalThrowable.getClass().getName());
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("exception unhandled", e);
        }
        if (originalThrowable instanceof Error) throw(Error) originalThrowable;
        if (originalThrowable instanceof RuntimeException) throw(RuntimeException) originalThrowable;
        if (originalThrowable instanceof ServletException) throw(ServletException) originalThrowable;
        if (originalThrowable instanceof IOException) throw(IOException) originalThrowable;
        throw new ServletException(originalThrowable);
    }

    /**
     * Get the original exception that caused the InvocationTargetException.
     *
     * @param e the InvocationTargetException
     * @return the original exception.
     * @throws javax.servlet.ServletException If the exception is a servlet exception, it will be thrown.
     */
    private Throwable getOriginalException(InvocationTargetException e) throws ServletException {
        Throwable originalThrowable = e.getTargetException();
        if (originalThrowable == null) {
            originalThrowable = e.getCause();
            if (originalThrowable == null) {
                throw new ServletException(e);
            }
        }
        // todo: is this right?
        // unwrap if exception is a ServletException
        if (originalThrowable instanceof ServletException) {
            ServletException se = (ServletException) originalThrowable;
            // go 5 deep to see if we can get a real exception
            for (int i = 0; i < 5 && se.getCause() instanceof ServletException; i++) {
                se = (ServletException) se.getCause();
            }
            if (se.getCause() instanceof InvocationTargetException) {
                return getOriginalException((InvocationTargetException) se.getCause());
            } else {
                throw se;
            }
        }
        return originalThrowable;
    }


    public Conf getConf() {
        return conf;
    }


    /**
     * Handles rewriting urls in jsp's etc, i.e. response.encodeURL() is overriden in the response wrapper.
     *
     * @param hsResponse          response
     * @param hsRequest           request
     * @param encodeUrlHasBeenRun if encodeUrl has already been run on the originalOutboundUrl speficy this to be true
     * @param outboundUrl         url
     * @return RewrittenOutboundUrl
     * @see UrlRewriteWrappedResponse
     */
    protected RewrittenOutboundUrl processEncodeURL(HttpServletResponse hsResponse, HttpServletRequest hsRequest,
                                                    boolean encodeUrlHasBeenRun, String outboundUrl) {

        if (log.isDebugEnabled()) {
            log.debug("processing outbound url for " + outboundUrl);
        }

        if (outboundUrl == null) {
            // this probably means encode called with no url
            return new RewrittenOutboundUrl(outboundUrl, true);
        }

        // attempt to match the rules
        boolean finalEncodeOutboundUrl = true;
        String finalToUrl = outboundUrl;
        final List outboundRules = conf.getOutboundRules();
        try {
            for (int i = 0; i < outboundRules.size(); i++) {
                final OutboundRule outboundRule = (OutboundRule) outboundRules.get(i);
                if (!encodeUrlHasBeenRun && outboundRule.isEncodeFirst()) {
                    continue;
                }
                if (encodeUrlHasBeenRun && !outboundRule.isEncodeFirst()) {
                    continue;
                }
                final RewrittenOutboundUrl rewrittenUrl = outboundRule.execute(finalToUrl, hsRequest, hsResponse);
                if (rewrittenUrl != null) {
                    // means this rule has matched
                    if (log.isDebugEnabled()) {
                        log.debug("\"" + outboundRule.getDisplayName() + "\" matched");
                    }
                    finalToUrl = rewrittenUrl.getTarget();
                    finalEncodeOutboundUrl = rewrittenUrl.isEncode();
                    if (outboundRule.isLast()) {
                        log.debug("rule is last");
                        // there can be no more matches on this request
                        break;
                    }
                }
            }
        } catch (InvocationTargetException e) {
            try {
                handleInvocationTargetException(hsRequest, hsResponse, e);
            } catch (ServletException e1) {
                log.error(e1);
            } catch (IOException e1) {
                log.error(e1);
            }
        }

        return new RewrittenOutboundUrl(finalToUrl, finalEncodeOutboundUrl);
    }

    /**
     * Destory the rewriter gracefully.
     */
    public void destroy() {
        conf.destroy();
    }

}
