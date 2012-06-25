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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * Handles DTD lookup and error handling for XML Conf parsing.
 *
 * @author Paul Tuckey
 * @version $Revision: 35 $ $Date: 2006-09-18 19:15:17 +1200 (Mon, 18 Sep 2006) $
 */
public class ConfHandler extends DefaultHandler {

    private static Log log = Log.getLog(ConfHandler.class);

    // pattern to match file://, http://, jndi://
    private static final Pattern HAS_PROTOCOL = Pattern.compile("^\\w+:");

    private String confSystemId;

    private static Hashtable dtdPaths = new Hashtable();

    static {
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 1.0//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite1.0.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.0//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.0.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.3//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.3.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.4//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.4.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.5//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.5.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.6//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.6.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 3.0//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite3.0.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 3.1//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite3.1.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 3.2//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite3.2.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 3.3//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite3.3.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 4.0//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite4.0.dtd");
    }

    public ConfHandler(String confSystemId) {
        this.confSystemId = confSystemId;
    }

    /**
     * Resolve the requested external entity.
     *
     * @param publicId The public identifier of the entity being referenced
     * @param systemId The system identifier of the entity being referenced
     * @throws org.xml.sax.SAXException if a parsing exception occurs
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException {
        if (publicId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't resolve entity with no publicId, system id is " + systemId);
            }
            if (confSystemId != null && !hasProtocol(systemId)) {
                return new InputSource(confSystemId.substring(0, confSystemId.lastIndexOf('/')) + "/" + systemId);
            }
            return new InputSource(systemId);
        }
        String entity = (String) dtdPaths.get(publicId);

        if (entity == null) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't resolve DTD: " + publicId + ", " + systemId);
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Resolving to DTD " + entity);
        }
        return new InputSource(ConfHandler.class.getResourceAsStream(entity));
    }

    /**
     * Check for protocol on a systemId.
     * eg, file://blah, http://blah, jndi://blah have protocols
     * /blah does not
     *
     * @param systemId the full systemId
     * @return true if systemId has protocol
     */
    private static boolean hasProtocol(String systemId) {
        return systemId != null && HAS_PROTOCOL.matcher(systemId).find();
    }

    //
    // ErrorHandler methods
    //

    public void warning(SAXParseException ex) {
        log.debug("error: " + ex.getMessage());
    }

    public void error(SAXParseException ex) {

        log.debug("error: " + ex.getMessage());
    }

    public void fatalError(SAXParseException ex) throws SAXException {
        log.debug("error: " + ex.getMessage());
    }


}
