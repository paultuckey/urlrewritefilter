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

import junit.framework.TestCase;
import org.tuckey.web.filters.urlrewrite.utils.Log;
import org.tuckey.web.filters.urlrewrite.utils.URLDecoder;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 */
public class UrlDecoderTest extends TestCase {

    final static String AllChars = "`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";

    public void setUp() {
        Log.setLevel("DEBUG");
    }

    public void testPathSpaces() throws URISyntaxException {
        String encoded = "/foo%20bar/foo+bar";
        String decoded = URLDecoder.decodeURL(encoded, "UTF-8");
        assertEquals("/foo bar/foo+bar", decoded);
    }

    public void testQuerySpacesFixed() throws URISyntaxException, UnsupportedEncodingException {
        String encoded = URLEncoder.encode("/foo bar/foo+bar", "UTF-8");
        String decoded = URLDecoder.decodeURL(encoded, "UTF-8");
        assertEquals("/foo+bar/foo+bar", decoded);
    }

    public void testQueryEncodingUTF8() throws URISyntaxException, UnsupportedEncodingException {
        String encoded = "/?foo=" + URLEncoder.encode(AllChars, "UTF-8");
        String decoded = URLDecoder.decodeURL(encoded, "UTF-8");
        assertEquals("/?foo=" + AllChars, decoded);
    }

    public void testQueryEncodingLatin1() throws URISyntaxException, UnsupportedEncodingException {
        String encoded = "/?foo=" + URLEncoder.encode(AllChars, "ISO-8859-1");
        String decoded = URLDecoder.decodeURL(encoded, "ISO-8859-1");
        assertEquals("/?foo=" + AllChars, decoded);
    }

    public void testRealEncoding() throws URISyntaxException, UnsupportedEncodingException {
        String katakana = "/???????????????" +
                "????????????????" +
                "????????????????" +
                "????????????????" +
                "????????????????" +
                "????????????????";
        String encoded = URLEncoder.encode(katakana, "UTF-8");
        String decoded = URLDecoder.decodeURL(encoded, "UTF-8");
        assertEquals(katakana, decoded);
    }

}

