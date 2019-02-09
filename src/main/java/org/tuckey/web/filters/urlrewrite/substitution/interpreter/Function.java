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

import org.tuckey.web.filters.urlrewrite.functions.StringFunctions;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionContext;
import org.tuckey.web.filters.urlrewrite.substitution.SubstitutionFilterChain;

/**
 * 
 */
public enum Function {

    REPLACE {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return REPLACEALL.execute(arg, ctx, nextFilter);
        }
    },

    REPLACEALL {
        @Override
        String execute(String args, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.replaceAll(args, nextFilter, ctx);
        }
    },

    REPLACEFIRST {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.replaceFirst(arg, nextFilter, ctx);

        }
    },
    ESCAPE {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.escape(arg, nextFilter, ctx);

        }
    },
    ESCAPEPATH {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.escapePath(arg, nextFilter, ctx);
        }
    },

    UNESCAPE {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.unescape(arg, nextFilter, ctx);
        }
    },
    UNESCAPEPATH {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.unescapePath(arg, nextFilter, ctx);
        }
    },
    LOWER {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return TOLOWER.execute(arg, ctx, nextFilter);
        }
    },
    UPPER {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return TOUPPER.execute(arg, ctx, nextFilter);
        }
    },
    TOLOWER {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.toLower(arg, nextFilter, ctx);

        }
    },
    TOUPPER {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.toUpper(arg, nextFilter, ctx);
        }
    },
    TRIM {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.trim(arg, nextFilter, ctx);
        }
    },

    LENGTH {
        @Override
        String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter) {
            return StringFunctions.length(arg, nextFilter, ctx);
        }
    };

    abstract String execute(String arg, SubstitutionContext ctx, SubstitutionFilterChain nextFilter);
}
