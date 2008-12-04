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
