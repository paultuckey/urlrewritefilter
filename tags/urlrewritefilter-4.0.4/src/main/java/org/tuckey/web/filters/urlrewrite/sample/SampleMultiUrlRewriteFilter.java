package org.tuckey.web.filters.urlrewrite.sample;

import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
import org.tuckey.web.filters.urlrewrite.UrlRewriter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
