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
package org.tuckey.web.filters.urlrewrite.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for matching server names.
 *
 * @author Paul Tuckey
 * @version $Revision: 36 $ $Date: 2006-09-19 18:32:39 +1200 (Tue, 19 Sep 2006) $
 */
public class ServerNameMatcher {

    private static Log log = Log.getLog(ServerNameMatcher.class);

    private List patterns = new ArrayList();
    WildcardHelper wh = new WildcardHelper();

    public ServerNameMatcher(String options) {
        String[] enableOnHostsArr = options.split(",");
        for (int i = 0; i < enableOnHostsArr.length; i++) {
            String s = enableOnHostsArr[i];
            if (StringUtils.isBlank(s)) continue;
            String rawPattern = StringUtils.trim(enableOnHostsArr[i]).toLowerCase();
            int[] compiledPattern = wh.compilePattern(rawPattern);
            patterns.add(compiledPattern);
        }
    }

    public boolean isMatch(String serverName) {
        log.debug("looking for hostname match on current server name " + serverName);
        if (patterns == null || StringUtils.isBlank(serverName)) {
            return false;
        }
        serverName = StringUtils.trim(serverName).toLowerCase();
        for (int i = 0; i < patterns.size(); i++) {
            int[] compiledPattern = (int[]) patterns.get(i);
            Map map = new HashMap();
            if (wh.match(map, serverName, compiledPattern)) return true;
        }
        return false;
    }

}
