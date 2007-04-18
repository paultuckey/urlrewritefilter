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

import junit.framework.TestCase;
import org.tuckey.web.testhelper.MockRequest;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class StatusTest extends TestCase {

    public void setUp() {
        Log.setLevel("DEBUG");
    }


    public void testSimple() throws IOException {

        MockRequest hsRequest = new MockRequest();
        InputStream is = ConfTest.class.getResourceAsStream(ConfTest.BASE_XML_PATH + "conf-test1.xml");
        Conf conf = new Conf(is, "conf-test1.xml");
        assertTrue(conf.isOk());
        UrlRewriter urlRewriter = new UrlRewriter(conf);
        UrlRewriteFilter urlRewriteFilter = new UrlRewriteFilter();

        Status status = new Status(urlRewriter.getConf(), urlRewriteFilter);
        status.displayStatusInContainer(hsRequest);

        // save it so we can view it
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File("status.html")));
        bos.write(status.getBuffer().toString().getBytes());

    }

}
