/**
 * Copyright (c) 2005-2023, Paul Tuckey
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * - Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution.
 * - Neither the name tuckey.org nor the names of its contributors
 * may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
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

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

public class URLDecoder {

	public static String decodeURL(String url, String charset) throws URISyntaxException{
		int queryPart = url.indexOf('?');
		String query = null;
		String path = url;
		if(queryPart != -1){
			query = url.substring(queryPart+1);
			path = url.substring(0, queryPart);
		}
		String decodedPath = decodePath(path, charset);
		if(query != null)
			return decodedPath + '?' + decodeQuery(query, charset);
		else
			return decodedPath;
	}

	public static String decodePath(String path, String charset) throws URISyntaxException{
		return decodeURLEncoded(path, false, charset);
	}

	public static String decodeQuery(String query, String charset) throws URISyntaxException{
		return decodeURLEncoded(query, true, charset);
	}

	public static String decodeURLEncoded(String part, boolean query, String charset) throws URISyntaxException{
		try{
			byte[] ascii = part.getBytes("ASCII");
			byte[] decoded = new byte[ascii.length];
			int j=0;
			for(int i=0;i<ascii.length;i++, j++){
				if(ascii[i] == '%'){
					if(i+2 >= ascii.length)
						throw new URISyntaxException(part, "Invalid URL-encoded string at char "+i);
					// get the next two bytes
					byte first = ascii[++i];
					byte second = ascii[++i];
					decoded[j] = (byte) ((hexToByte(first) * 16) + hexToByte(second));
				}else if(query && ascii[i] == '+')
					decoded[j] = ' ';
				else
					decoded[j] = ascii[i];
			}
			// now decode
			return new String(decoded, 0, j, charset);
		}catch(UnsupportedEncodingException x){
			throw new URISyntaxException(part, "Invalid encoding: "+charset);
		}
	}


	private static byte hexToByte(byte b) throws URISyntaxException{
		switch(b){
		case '0': return 0;
		case '1': return 1;
		case '2': return 2;
		case '3': return 3;
		case '4': return 4;
		case '5': return 5;
		case '6': return 6;
		case '7': return 7;
		case '8': return 8;
		case '9': return 9;
		case 'a':
		case 'A': return 10;
		case 'b':
		case 'B': return 11;
		case 'c':
		case 'C': return 12;
		case 'd':
		case 'D': return 13;
		case 'e':
		case 'E': return 14;
		case 'f':
		case 'F': return 15;
		}
		throw new URISyntaxException(String.valueOf(b), "Invalid URL-encoded string");
	}
}
