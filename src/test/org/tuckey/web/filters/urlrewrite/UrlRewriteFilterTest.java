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
import org.tuckey.web.testhelper.MockFilterConfig;
import org.tuckey.web.testhelper.MockServletContext;
import org.tuckey.web.filters.urlrewrite.utils.Log;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class UrlRewriteFilterTest extends TestCase {

    private UrlRewriteFilter filter;

    public void setUp() {
        Log.setLevel("stdout:TRACE");
        filter = new UrlRewriteFilter();
        Log.setLevel("stdout:TRACE");
    }

    public void tearDown() {
        filter.destroy();
        filter = null;
    }

    public void testInit() {
        filter.init(null);
        filter.init(new MockFilterConfig());
    }

    public void testInitContext() {
        MockFilterConfig mockFilterConfig = new MockFilterConfig();
        mockFilterConfig.setServletContext(new MockServletContext());
        filter.init(mockFilterConfig);
    }

}
