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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;

/**
 * Handles DTD lookup and error handling for XML Conf parsing.
 *
 * @author Paul Tuckey
 * @version $Revision: 35 $ $Date: 2006-09-18 19:15:17 +1200 (Mon, 18 Sep 2006) $
 */
public class ConfHandler extends DefaultHandler {

    private static Log log = Log.getLog(ConfHandler.class);

    private static Hashtable dtdPaths = new Hashtable();
    private String confSystemId;

    static {
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 1.0//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite1.0.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.0//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.0.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.3//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.3.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.4//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.4.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.5//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.5.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 2.6//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite2.6.dtd");
        dtdPaths.put("-//tuckey.org//DTD UrlRewrite 3.0//EN", "/org/tuckey/web/filters/urlrewrite/dtds/urlrewrite3.0.dtd");
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
        if ( publicId == null ) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't resolve entity with no publicId, system id is " + systemId);
            }
            if ( confSystemId != null && !systemId.startsWith("file:") ) {
                return new InputSource(confSystemId.substring(0, confSystemId.lastIndexOf('/')) + "/" + systemId); 
            }
            return new InputSource(systemId);
            //return null;
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
