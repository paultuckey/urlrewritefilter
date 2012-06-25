package org.tuckey.web.filters.urlrewrite.substitution;

import javax.servlet.ServletContext;
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
