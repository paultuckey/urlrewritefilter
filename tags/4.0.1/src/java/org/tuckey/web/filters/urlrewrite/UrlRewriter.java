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

import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.StringUtils;
import org.tuckey.web.filters.urlrewrite.utils.URLDecoder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
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


    /**
     * Return the path within the web application for the given request.
     * <p>Detects include request URL if called within a RequestDispatcher include.
     */
    public String getPathWithinApplication(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri == null) requestUri = "";
        String decodedRequestUri = decodeRequestString(request, requestUri);
        String contextPath = getContextPath(request);
        String path;
        if (StringUtils.startsWithIgnoreCase(decodedRequestUri, contextPath) && !conf.isUseContext()) {
            // Normal case: URI contains context path.
            path = decodedRequestUri.substring(contextPath.length());

        } else if (!StringUtils.startsWithIgnoreCase(decodedRequestUri, contextPath) && conf.isUseContext()) {
            // add the context path on
            path = contextPath + decodedRequestUri;

        } else {
            path = decodedRequestUri;
        }
        return StringUtils.isBlank(path) ? "/" : path;
    }

    /**
     * Return the context path for the given request, detecting an include request
     * URL if called within a RequestDispatcher include.
     * <p>As the value returned by <code>request.getContextPath()</code> is <i>not</i>
     * decoded by the servlet container, this method will decode it.
     */
    public String getContextPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        if ("/".equals(contextPath)) {
            // Invalid case, but happens for includes on Jetty: silently adapt it.
            contextPath = "";
        }
        return decodeRequestString(request, contextPath);
    }

    /**
     * Decode the string with a URLDecoder. The encoding will be taken
     * from the request, falling back to the default for your platform ("ISO-8859-1" on windows).
     */
    public String decodeRequestString(HttpServletRequest request, String source) {
        if (conf.isDecodeUsingEncodingHeader()) {
            String enc = request.getCharacterEncoding();
            if (enc != null) {
                try {
                    return URLDecoder.decodeURL(source, enc);
                } catch (URISyntaxException ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not decode: " + source + " (header encoding: '" + enc + "'); exception: " + ex.getMessage());
                    }
                }
            }
        }
        if (conf.isDecodeUsingCustomCharsetRequired()) {
            String enc = conf.getDecodeUsing();
            if (enc != null) {
                try {
                    return URLDecoder.decodeURL(source, enc);
                } catch (URISyntaxException ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Could not decode: " + source + " (encoding: '" + enc + "') using default encoding; exception: " + ex.getMessage());
                    }
                }
            }
        }
        return source;
    }


    private RuleChain getNewChain(final HttpServletRequest hsRequest, FilterChain parentChain) {

        String originalUrl = getPathWithinApplication(hsRequest);

        if (originalUrl == null) {
            // for some reason the engine is not giving us the url
            // this isn't good
            log.debug("unable to fetch request uri from request.  This shouldn't happen, it may indicate that " +
                    "the web application server has a bug or that the request was not pased correctly.");
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("processing request for " + originalUrl);
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
        if (originalThrowable instanceof Error) throw (Error) originalThrowable;
        if (originalThrowable instanceof RuntimeException) throw (RuntimeException) originalThrowable;
        if (originalThrowable instanceof ServletException) throw (ServletException) originalThrowable;
        if (originalThrowable instanceof IOException) throw (IOException) originalThrowable;
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
            return new RewrittenOutboundUrl(null, true);
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



