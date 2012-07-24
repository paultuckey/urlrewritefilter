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

import java.util.regex.Matcher;

/**
 * Simple wrapper for java.util.regex.Matcher.
 *
 * @see java.util.regex.Matcher
 */
public class RegexMatcher implements StringMatchingMatcher {

    private Matcher matcher;
    private boolean found = false;

    public RegexMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    /**
     * @see Matcher#find
     */
    public boolean find() {
        found = matcher.find();
        return found;
    }

    public boolean isFound() {
        return found;
    }

    public void reset() {
        matcher.reset();
        found = false;
    }

    public String replaceAll(String replacement) {
        String replaced = matcher.replaceAll(replacement);
        reset();
        return replaced;
    }

    public int groupCount() {
        return matcher.groupCount();
    }

    public String group(int groupId) {
        return matcher.group(groupId);
    }

	public int end() {
		return matcher.end();
	}

	public int start() {
		return matcher.start();
	}

	public boolean isMultipleMatchingSupported() {
		return true;
	}


}
