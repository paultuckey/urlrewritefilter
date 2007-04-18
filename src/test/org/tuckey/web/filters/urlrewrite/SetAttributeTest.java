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
import org.tuckey.web.testhelper.MockResponse;
import org.tuckey.web.filters.urlrewrite.utils.Log;

import javax.servlet.http.Cookie;

/**
 * @author Paul Tuckey
 * @version $Revision: 1 $ $Date: 2006-08-01 21:40:28 +1200 (Tue, 01 Aug 2006) $
 */
public class SetAttributeTest extends TestCase {

    public void setUp() {
        Log.setLevel("DEBUG");
    }


    public void testBasic() {
        SetAttribute set = new SetAttribute();
        set.setName("blah");
        set.setValue("mercuryrev");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();

        set.initialise();
        set.execute(request, response);
        assertTrue(request.getAttribute("blah").equals("mercuryrev"));

    }

    public void testTypeStatus() {
        SetAttribute set = new SetAttribute();
        set.setType("status");
        set.setValue("999");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        set.initialise();
        set.execute(request, response);
        assertEquals(999, response.getStatus());
    }

    public void testTypeCookie() {
        SetAttribute set = new SetAttribute();
        set.setType("cookie");
        set.setName("mycook");
        set.setValue("someval dasd:blah.com:89009:/ass");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        set.initialise();
        set.execute(request, response);
        Cookie cookie = (Cookie) response.getCookies().get(0);
        assertEquals("blah.com", cookie.getDomain());
        assertEquals(89009, cookie.getMaxAge());
        assertEquals("someval dasd", cookie.getValue());
        assertEquals("/ass", cookie.getPath());
    }

    public void testTypeLocale() {
        SetAttribute set = new SetAttribute();
        set.setType("locale");
        set.setValue("en");
        assertTrue(set.initialise());

        set.setValue("en-NZ");
        assertTrue(set.initialise());

        set.setValue("en-NZ-slang");
        MockRequest request = new MockRequest();
        MockResponse response = new MockResponse();
        set.initialise();
        set.execute(request, response);
        assertEquals("slang", response.getLocale().getVariant());
    }


}
