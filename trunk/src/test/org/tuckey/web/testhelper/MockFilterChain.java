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
package org.tuckey.web.testhelper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Paul Tuckey
 * @version $Revision: 26 $ $Date: 2006-09-05 22:21:17 +1200 (Tue, 05 Sep 2006) $
 */
public class MockFilterChain implements FilterChain {

    private int invocationCount = 0;
    private long timeInvoked = 0;

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
        invocationCount++;
        timeInvoked = System.currentTimeMillis();
        // make sure we wait a little so time elapses
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            //
        }
    }

    public boolean isDoFilterRun() {
        return invocationCount > 0;
    }

    public int getInvocationCount() {
        return invocationCount;
    }

    public long getTimeInvoked() {
        return timeInvoked;
    }
}
