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
package org.tuckey.web.filters.urlrewrite.substitution.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tokenize &quot;to&quot; value.
 * 
 */
public class Tokenizer {
    static final String FUNC_START = "${";
    static final String FUNC_END = "}";
    static final String PARAM_DELIMITER = ":";

    private static final String[] TOKEN_DELIITER_ARY = { FUNC_START, FUNC_END, PARAM_DELIMITER };

    List<String> tokens = new ArrayList<String>();
    int pos = 0;

    Tokenizer(String src) {
        int currentPos = 0;
        int startPos = 0;
        int endPos = 0;
        int paramPos = 0;

        while (currentPos < src.length()) {

            startPos = src.indexOf(FUNC_START, currentPos);
            endPos = src.indexOf(FUNC_END, currentPos);
            paramPos = src.indexOf(PARAM_DELIMITER, currentPos);

            Set<Integer> posSet = new HashSet<Integer>();

            posSet.add(startPos);
            posSet.add(endPos);
            posSet.add(paramPos);

            posSet.remove(-1);
            if (posSet.isEmpty()) {
                tokens.add(src.substring(currentPos));
                break;
            }

            List<Integer> posList = new ArrayList<Integer>(posSet);
            int candidatePos = Collections.min(posList);
            if (candidatePos != 0 && currentPos != candidatePos) {
                String newToken = src.substring(currentPos, candidatePos);
                if (src.charAt(candidatePos) == FUNC_END.charAt(0) && newToken.contains("%{")) {
                    newToken = newToken + FUNC_END;
                    candidatePos++;
                }
                tokens.add(newToken);
                currentPos += newToken.length();
            }
            if (candidatePos != src.length()) {
                for (String tokenDelimiter : TOKEN_DELIITER_ARY) {
                    if (src.charAt(candidatePos) == tokenDelimiter.charAt(0)) {
                        tokens.add(tokenDelimiter);
                        currentPos += tokenDelimiter.length();
                    }
                }
            }
        }
    }

    public boolean hasNext() {
        return pos != tokens.size();
    }

    public String next() {
        String candidate = tokens.get(pos);
        pos++;
        return candidate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Tokenizer [tokens=");
        builder.append(tokens);
        builder.append(", pos=");
        builder.append(pos);
        builder.append("]");
        return builder.toString();
    }

}
