package org.tuckey.web.filters.urlrewrite.utils;


import org.tuckey.noclash.gzipfilter.GzipFilter;
import org.tuckey.noclash.gzipfilter.integration.GzipFilterIntegration;
import org.tuckey.noclash.gzipfilter.selector.GzipCompatibilitySelector;
import org.tuckey.noclash.gzipfilter.selector.GzipCompatibilitySelectorFactory;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 *
 *
 * use like this with lots of conditions:
 *  <rule>
        <condition type="mime-type">text/.*|application/x-javascript|application/javascript|application/xml|application/xhtml\+xml</condition>

        <!-- be paranoid about not zipping images -->
        <condition type="request-url" operator="notequal">.*\.png(\?|$)</condition>
        <condition type="request-url" operator="notequal">.*\.gif(\?|$)</condition>
        <condition type="request-url" operator="notequal">.*\.jpe?g(\?|$)</condition>

        <!-- ignore Internet Explorer 4-6. Let IE 7 be compressed -->
        <condition name="user-agent" operator="notequal">.*MSIE [4-6].*</condition>
        <!-- exclude Mozilla/4.0, as MSIE pretends to be that -->
        <condition name="user-agent" operator="notequal">Mozilla/4\.[1-9].*</condition>
        <!-- don't gzip anything for Netscape Navigtor -->
        <condition name="user-agent" operator="notequal">Mozilla/[2-3].*</condition>

        <from>^/gzip-me-conditionally/.*$</from>
        <gzip/>
    </rule>
 *
 */
public class GzipFilterRun extends GzipFilter {

    public GzipFilterRun() {
        super(new GzipFilterIntegration() {
            public boolean useGzip() {
                return true;
            }

            public String getResponseEncoding(HttpServletRequest request) {
                return "UTF-8"; // todo: decide what this should be?
            }
        });
    }

    public void run(HttpServletRequest servletRequest, HttpServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        super.doFilter(servletRequest, servletResponse, filterChain);
    }

    protected GzipCompatibilitySelectorFactory getFactory() {
        return new GzipCompatibilitySelectorFactory() {
            public GzipCompatibilitySelector getSelector(FilterConfig filterConfig, HttpServletRequest request) {
                return new GzipCompatibilitySelector() {
                    public boolean shouldGzip(String contentType) {
                        return true;
                    }

                    public boolean shouldGzip() {
                        return true;
                    }
                };
            }
        };
    }


}
