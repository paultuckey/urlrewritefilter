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
package org.tuckey.web.filters.urlrewrite.substitution;

import jakarta.servlet.ServletContext;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class ChainedSubstitutionFilters implements SubstitutionFilterChain {

	private List filters;
	private int nextFilter = 0;
	
	public ChainedSubstitutionFilters(List filters) {
		this.filters = filters;
	}

	public String substitute(String string, SubstitutionContext ctx){
		if(nextFilter >= filters.size())
			return string;
		String ret = ((SubstitutionFilter)filters.get(nextFilter++)).substitute(string, ctx, this);
		nextFilter--;
		return ret;
	}
	
	public static String substitute(String string, SubstitutionFilter singleFilter){
		ArrayList list = new ArrayList(1);
		list.add(singleFilter);
		SubstitutionFilterChain filterChain = new ChainedSubstitutionFilters(list);
		return filterChain.substitute(string, null);
	}
	
	public static SubstitutionFilterChain getDefaultSubstitutionChain(
			boolean withPattern, boolean withFunction, boolean withVariable,
			boolean withBackReference) {
        return getDefaultSubstitutionChain(withPattern, withFunction, withVariable, withBackReference, null);
	}
	
	public static SubstitutionFilterChain getDefaultSubstitutionChain(boolean withPattern, boolean withFunction, boolean withVariable, boolean withBackReference, ServletContext sc){
        List substitutionFilters = new LinkedList();
        
        if(withPattern)
        	substitutionFilters.add(new PatternReplacer());
        if(withFunction)
        	substitutionFilters.add(new FunctionReplacer());
        if(withVariable)
			substitutionFilters.add(sc == null ? new VariableReplacer() : new VariableReplacer(sc));
        if(withBackReference)
        	substitutionFilters.add(new BackReferenceReplacer());
        substitutionFilters.add(new MatcherReplacer());
        substitutionFilters.add(new UnescapeReplacer());

        return new ChainedSubstitutionFilters(substitutionFilters);
	}
}
