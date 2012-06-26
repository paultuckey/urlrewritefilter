package org.tuckey.web.filters.urlrewriteviacontainer;

import org.tuckey.web.filters.urlrewrite.annotation.HttpUrl;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Paul Tuckey
 * @author $Author: $
 * @author $Id: $
 */
public class AnnotatedClassSample {

    @HttpUrl("^/do-something/([0-9]+)$")
    public void doSomething(Long id, HttpServletResponse response) throws IOException {
        response.getWriter().print("AnnotatedClassSample id=" + id);
    }

}
