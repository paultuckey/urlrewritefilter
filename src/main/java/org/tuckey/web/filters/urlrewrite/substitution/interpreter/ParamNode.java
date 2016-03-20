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

import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionContext;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionFilterChain;

import java.util.ArrayList;
import java.util.List;

/**
 * &lt;param&gt; ::= &lt;element&gt; * | &lt;element&gt; * : &lt;param&gt;
 * 
 */
public class ParamNode implements Node {
    List<ElementNode> elementList;
    ParamNode secondElement;

    SubstitutionContext ctx;
    SubstitutionFilterChain nextFilter;

    ParamNode(SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
        this.ctx = ctx;
        this.nextFilter = nextFilter;
        this.elementList = new ArrayList<ElementNode>();
    }

    public void parse(Context context) throws ParseException {
        while (true) {
            if (context.currentToken() != null) {
                if (context.currentToken().equals(Tokenizer.FUNC_START)) {
                    ElementNode en = new ElementNode(ctx, nextFilter);
                    en.parse(context);
                    elementList.add(en);
                } else if (context.currentToken().equals(Tokenizer.PARAM_DELIMITER)) {
                    context.skipToken(Tokenizer.PARAM_DELIMITER);
                    secondElement = new ParamNode(ctx, nextFilter);
                    secondElement.parse(context);
                } else if (context.currentToken().equals(Tokenizer.FUNC_END)) {
                    break;
                } else {
                    ElementNode en = new ElementNode(ctx, nextFilter);
                    en.parse(context);
                    elementList.add(en);
                }
            } else {
                break;
            }
        }
    }

    public String evaluate() {
        StringBuilder sb = new StringBuilder();
        for (ElementNode en : elementList) {
            sb.append(en.evaluate());
        }
        if (secondElement != null) {
            sb.append(Tokenizer.PARAM_DELIMITER);
            sb.append(secondElement.evaluate());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ElementNode en : elementList) {
            sb.append(en.toString());
        }
        if (secondElement != null) {
            sb.append(Tokenizer.PARAM_DELIMITER);
            sb.append(secondElement.toString());
        }
        return sb.toString();
    }
}
